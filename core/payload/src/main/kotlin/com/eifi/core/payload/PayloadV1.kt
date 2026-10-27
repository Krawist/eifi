package com.eifi.core.payload

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.time.Instant
import java.util.zip.CRC32

/**
 * Binary layout of the v1 payment data, in order:
 * - id: 6 bytes;
 * - expiry: epoch seconds, unsigned 32-bit big-endian;
 * - amount: unsigned varint;
 * - display name: 1-byte length + UTF-8;
 * - receiving methods: 1-byte count, then for each a header byte (operator wire code in bits 4 to 7,
 *   method wire code in bits 1 to 3, numeric flag in bit 0) and the value, as 1-byte digit count + packed
 *   digits when numeric, 1-byte length + UTF-8 otherwise; a phone number is written without its leading "+";
 * - CRC-32 of everything before it, unsigned 32-bit big-endian.
 */
internal object PayloadV1 {
    fun write(request: PaymentRequest): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(request.id.chunked(2).map { it.toInt(radix = 16).toByte() }.toByteArray())
        out.writeUInt32(request.expiresAt.epochSecond)
        out.writeVarint(request.amount)
        out.writeText(request.displayName)
        out.write(request.receivingMethods.size)
        request.receivingMethods.forEach { (operator, method) -> out.writeMethod(operator, method) }
        val body = out.toByteArray()
        return body + ByteArrayOutputStream().apply { writeUInt32(crc32(body)) }.toByteArray()
    }

    /** @throws MalformedPayloadException when [bytes] is not a valid v1 payload. */
    fun read(bytes: ByteArray): PaymentRequest {
        if (bytes.size < UINT32_BYTES) throw MalformedPayloadException(MalformedReason.Truncated)
        val body = bytes.copyOfRange(0, bytes.size - UINT32_BYTES)
        val checksum = Reader(bytes.copyOfRange(body.size, bytes.size)).readUInt32()
        if (checksum != crc32(body)) throw MalformedPayloadException(MalformedReason.ChecksumMismatch)
        return Reader(body).readRequest()
    }

    /** Identifies the method in the URL: must never change. 0 and 3 to 7 are free for new methods. */
    private val ReceivingMethod.wireCode: Int
        get() = when (this) {
            is ReceivingMethod.MerchantCode -> MERCHANT_CODE
            is ReceivingMethod.PhoneNumber -> PHONE_NUMBER
        }

    private fun crc32(bytes: ByteArray): Long = CRC32().apply { update(bytes) }.value

    private fun ByteArrayOutputStream.writeUInt32(value: Long) {
        require(value in 0..MAX_UINT32) { "$value does not fit in 32 bits" }
        for (shift in UINT32_SHIFTS) write((value shr shift).toInt() and BYTE_MASK)
    }

    private fun ByteArrayOutputStream.writeVarint(value: Long) {
        var remaining = value
        while (remaining >= VARINT_CONTINUATION) {
            write((remaining and VARINT_PAYLOAD_MASK.toLong()).toInt() or VARINT_CONTINUATION)
            remaining = remaining ushr VARINT_SHIFT
        }
        write(remaining.toInt())
    }

    private fun ByteArrayOutputStream.writeText(text: String) {
        val bytes = text.encodeToByteArray()
        require(bytes.size <= MAX_LENGTH) { "\"$text\" is longer than $MAX_LENGTH bytes" }
        write(bytes.size)
        write(bytes)
    }

    private fun ByteArrayOutputStream.writeMethod(operator: Operator, method: ReceivingMethod) {
        val value = if (method is ReceivingMethod.PhoneNumber) method.value.removePrefix(E164_PREFIX) else method.value
        val numeric = value.length <= MAX_LENGTH && value.all { it in '0'..'9' }
        var header = (operator.wireCode shl NIBBLE_SHIFT) or (method.wireCode shl METHOD_SHIFT)
        if (numeric) header = header or NUMERIC_FLAG
        write(header)
        if (numeric) writeDigits(value) else writeText(value)
    }

    /** Two digits per byte, the last low nibble filled with [DIGIT_FILLER] when the count is odd. */
    private fun ByteArrayOutputStream.writeDigits(digits: String) {
        write(digits.length)
        digits.chunked(2).forEach { pair ->
            val high = pair[0].digitToInt()
            val low = pair.getOrNull(1)?.digitToInt() ?: DIGIT_FILLER
            write((high shl NIBBLE_SHIFT) or low)
        }
    }

    private class Reader(private val bytes: ByteArray) {
        private var position = 0

        fun readRequest(): PaymentRequest {
            val id = readBytes(PaymentRequestService.ID_BYTES).joinToString(separator = "") { "%02x".format(it) }
            val expiresAt = Instant.ofEpochSecond(readUInt32())
            val amount = readVarint()
            val displayName = readText()
            val count = readByte()
            val methods = List(count) { readMethod() }.toMap()
            if (methods.size != count || position != bytes.size) invalidContent()
            return content {
                PaymentRequest(id, displayName, amount, methods, expiresAt).also(PaymentRequestService::validate)
            }
        }

        fun readUInt32(): Long = readBytes(UINT32_BYTES).fold(0L) { value, byte ->
            (value shl Byte.SIZE_BITS) or (byte.toLong() and BYTE_MASK.toLong())
        }

        private fun readMethod(): Pair<Operator, ReceivingMethod> {
            val header = readByte()
            val operator = Operator.entries.firstOrNull { it.wireCode == header shr NIBBLE_SHIFT } ?: invalidContent()
            val methodCode = (header shr METHOD_SHIFT) and METHOD_MASK
            val value = if (header and NUMERIC_FLAG != 0) readDigits() else readText()
            val method = when (methodCode) {
                MERCHANT_CODE -> ReceivingMethod.MerchantCode(value)
                PHONE_NUMBER -> ReceivingMethod.PhoneNumber(E164_PREFIX + value)
                else -> invalidContent()
            }
            return operator to method
        }

        private fun readVarint(): Long {
            var value = 0L
            var shift = 0
            while (true) {
                if (shift > MAX_VARINT_SHIFT) invalidContent()
                val byte = readByte()
                value = value or ((byte and VARINT_PAYLOAD_MASK).toLong() shl shift)
                if (byte and VARINT_CONTINUATION == 0) return value
                shift += VARINT_SHIFT
            }
        }

        private fun readText(): String {
            val decoder = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            return try {
                decoder.decode(ByteBuffer.wrap(readBytes(readByte()))).toString()
            } catch (e: CharacterCodingException) {
                throw MalformedPayloadException(MalformedReason.InvalidContent, e)
            }
        }

        private fun readDigits(): String {
            val count = readByte()
            val digits = readBytes((count + 1) / 2).flatMap { byte ->
                val value = byte.toInt() and BYTE_MASK
                listOf(value shr NIBBLE_SHIFT, value and NIBBLE_MASK)
            }
            if (digits.take(count).any { it > MAX_DIGIT }) invalidContent()
            return digits.take(count).joinToString(separator = "")
        }

        private fun readByte(): Int = readBytes(1)[0].toInt() and BYTE_MASK

        private fun readBytes(count: Int): ByteArray {
            if (position + count > bytes.size) throw MalformedPayloadException(MalformedReason.Truncated)
            return bytes.copyOfRange(position, position + count).also { position += count }
        }

        /** Maps the validation of [PaymentRequestService] to [MalformedReason.InvalidContent]. */
        private fun <T> content(build: () -> T): T = try {
            build()
        } catch (e: IllegalArgumentException) {
            throw MalformedPayloadException(MalformedReason.InvalidContent, e)
        }

        private fun invalidContent(): Nothing = throw MalformedPayloadException(MalformedReason.InvalidContent)
    }

    private const val BYTE_MASK = 0xFF
    private const val MAX_LENGTH = 0xFF
    private const val MAX_UINT32 = 0xFFFF_FFFFL
    private const val UINT32_BYTES = 4
    private val UINT32_SHIFTS = listOf(24, 16, 8, 0)
    private const val VARINT_PAYLOAD_MASK = 0x7F
    private const val VARINT_CONTINUATION = 0x80
    private const val VARINT_SHIFT = 7
    private const val MAX_VARINT_SHIFT = 63
    private const val NIBBLE_SHIFT = 4
    private const val NIBBLE_MASK = 0x0F
    private const val METHOD_SHIFT = 1
    private const val METHOD_MASK = 0x07
    private const val MERCHANT_CODE = 1
    private const val PHONE_NUMBER = 2
    private const val E164_PREFIX = "+"
    private const val NUMERIC_FLAG = 0x01
    private const val DIGIT_FILLER = 0x0F
    private const val MAX_DIGIT = 9
}
