package com.eifi.core.nfc

/**
 * NFC Forum Type 4 tag holding [ndefMessage], answering the commands of a reader (ISO 7816-4 APDUs): select the
 * NDEF application, select the capability container or the NDEF file, read binary. The tag is read-only.
 */
internal class Type4Tag(ndefMessage: ByteArray) {

    /** NDEF file: 2-byte length, then the message. */
    private val ndefFile = byteArrayOf((ndefMessage.size shr 8).toByte(), ndefMessage.size.toByte()) + ndefMessage

    private var applicationSelected = false
    private var selectedFile: ByteArray? = null

    /** Response to [command], ending with its status word. */
    fun process(command: ByteArray): ByteArray = when {
        command.startsWith(SELECT_APPLICATION) -> {
            applicationSelected = true
            selectedFile = null
            OK
        }
        !applicationSelected -> FILE_NOT_FOUND
        command.startsWith(SELECT_CAPABILITY_CONTAINER) -> select(CAPABILITY_CONTAINER)
        command.startsWith(SELECT_NDEF_FILE) -> select(ndefFile)
        command.size >= READ_BINARY_HEADER && command[0] == CLA && command[1] == INS_READ_BINARY -> readBinary(command)
        else -> INSTRUCTION_NOT_SUPPORTED
    }

    /** Back to the initial state, once the reader has left the field. */
    fun reset() {
        applicationSelected = false
        selectedFile = null
    }

    private fun select(file: ByteArray): ByteArray {
        selectedFile = file
        return OK
    }

    private fun readBinary(command: ByteArray): ByteArray {
        val file = selectedFile ?: return FILE_NOT_FOUND
        val offset = (command[P1].toInt() and BYTE_MASK shl Byte.SIZE_BITS) or (command[P2].toInt() and BYTE_MASK)
        // Le absent or 0 means as much as possible (256 bytes in short APDUs).
        val requested = command.getOrNull(READ_BINARY_HEADER)?.toInt()?.and(BYTE_MASK)?.takeIf { it > 0 } ?: MAX_LE
        return if (offset > file.size) {
            WRONG_OFFSET
        } else {
            file.copyOfRange(offset, minOf(file.size, offset + requested)) + OK
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    internal companion object {
        private const val BYTE_MASK = 0xFF
        private const val CLA: Byte = 0x00
        private const val INS_READ_BINARY: Byte = 0xB0.toByte()
        private const val P1 = 2
        private const val P2 = 3
        private const val READ_BINARY_HEADER = 4
        private const val MAX_LE = 256

        /** SELECT by name of the NDEF Tag Application, AID D2760000850101. */
        private val SELECT_APPLICATION = bytes(0x00, 0xA4, 0x04, 0x00, 0x07, 0xD2, 0x76, 0x00, 0x00, 0x85, 0x01, 0x01)

        /** SELECT by file identifier E103 (capability container). */
        private val SELECT_CAPABILITY_CONTAINER = bytes(0x00, 0xA4, 0x00, 0x0C, 0x02, 0xE1, 0x03)

        /** SELECT by file identifier E104 (NDEF file). */
        private val SELECT_NDEF_FILE = bytes(0x00, 0xA4, 0x00, 0x0C, 0x02, 0xE1, 0x04)

        /**
         * Capability container: length 15, mapping version 2.0, MLe 255 and MLc 255 bytes, then the NDEF file
         * control TLV: file E104, at most 1024 bytes, read granted, write denied.
         */
        private val CAPABILITY_CONTAINER = bytes(
            0x00, 0x0F, 0x20, 0x00, 0xFF, 0x00, 0xFF,
            0x04, 0x06, 0xE1, 0x04, 0x04, 0x00, 0x00, 0xFF,
        )

        private val OK = bytes(0x90, 0x00)
        private val WRONG_OFFSET = bytes(0x6B, 0x00)
        private val INSTRUCTION_NOT_SUPPORTED = bytes(0x6D, 0x00)

        /** Also the answer when no payment is emitted, so that the reader moves on. */
        val FILE_NOT_FOUND = bytes(0x6A, 0x82)

        private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
    }
}
