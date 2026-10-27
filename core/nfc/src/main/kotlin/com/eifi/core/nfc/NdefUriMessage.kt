package com.eifi.core.nfc

/**
 * NDEF message holding [url] as a single URI record (NFC Forum RTD URI), "https://" abbreviated to its
 * identifier code.
 */
internal fun ndefUriMessage(url: String): ByteArray {
    val prefix = URI_PREFIXES.entries.firstOrNull { url.startsWith(it.value) }
    val payload = byteArrayOf((prefix?.key ?: NO_PREFIX).toByte()) +
        url.removePrefix(prefix?.value.orEmpty()).encodeToByteArray()
    val short = payload.size <= MAX_SHORT_PAYLOAD
    val header = MESSAGE_BEGIN or MESSAGE_END or TNF_WELL_KNOWN or (if (short) SHORT_RECORD else 0)
    val length = if (short) {
        byteArrayOf(payload.size.toByte())
    } else {
        ByteArray(LONG_LENGTH_BYTES) { (payload.size shr (Byte.SIZE_BITS * (LONG_LENGTH_BYTES - 1 - it))).toByte() }
    }
    return byteArrayOf(header.toByte(), TYPE_LENGTH.toByte()) + length + URI_TYPE + payload
}

private const val MESSAGE_BEGIN = 0x80
private const val MESSAGE_END = 0x40
private const val SHORT_RECORD = 0x10
private const val TNF_WELL_KNOWN = 0x01
private const val TYPE_LENGTH = 1
private const val MAX_SHORT_PAYLOAD = 0xFF
private const val LONG_LENGTH_BYTES = 4
private val URI_TYPE = byteArrayOf('U'.code.toByte())
private const val NO_PREFIX = 0x00

/** URI identifier codes of the RTD URI specification used by Eifi URLs. */
private val URI_PREFIXES = mapOf(0x04 to "https://", 0x03 to "http://")
