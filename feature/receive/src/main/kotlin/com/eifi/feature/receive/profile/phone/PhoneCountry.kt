package com.eifi.feature.receive.profile.phone

/**
 * A country whose phone numbers can be entered: shown with its [flag] and [callingCode], or its [regionCode]
 * as abbreviation, and the rules of its numbering plan.
 */
internal interface PhoneCountry {
    /** ISO 3166-1 alpha-2 code, e.g. "CM", also used as the country abbreviation. */
    val regionCode: String

    /** International calling code, e.g. "+237". */
    val callingCode: String

    /** Digits of a national number, where the input stops. */
    val nationalNumberLength: Int

    /** Emoji flag, made of the regional indicator symbols of [regionCode]. */
    val flag: String
        get() = regionCode.uppercase().map { String(Character.toChars(REGIONAL_INDICATOR_A + (it - 'A'))) }
            .joinToString(separator = "")

    /** The national digits of what the user typed or pasted, without spaces nor international prefix. */
    fun normalize(input: String): String

    /** Whether [nationalNumber] is a number of this country that can receive money. */
    fun isValid(nationalNumber: String): Boolean

    /** A valid [nationalNumber] in E.164 format, as it is stored: e.g. "+237690123456". */
    fun toE164(nationalNumber: String): String

    /** The national number of [e164] when it is a valid number of this country, otherwise `null`. */
    fun nationalNumberOf(e164: String): String?
}

private const val REGIONAL_INDICATOR_A = 0x1F1E6
