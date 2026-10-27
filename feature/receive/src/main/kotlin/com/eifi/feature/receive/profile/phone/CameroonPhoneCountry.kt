package com.eifi.feature.receive.profile.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

/**
 * Cameroon: mobile numbers only, checked against the numbering plan of Google's libphonenumber. The operator of
 * the prefix is not checked: it is only confirmed by EIFI-001.
 */
internal object CameroonPhoneCountry : PhoneCountry {
    override val regionCode = "CM"
    override val callingCode = "+237"
    override val nationalNumberLength = 9

    private val internationalPrefixes = listOf("00237", "237")
    private val mobileTypes = setOf(PhoneNumberType.MOBILE, PhoneNumberType.FIXED_LINE_OR_MOBILE)

    override fun normalize(input: String): String {
        val digits = input.filter(Char::isDigit)
        val prefix = internationalPrefixes.firstOrNull { digits.length > nationalNumberLength && digits.startsWith(it) }
        return digits.removePrefix(prefix.orEmpty()).take(nationalNumberLength)
    }

    override fun toE164(nationalNumber: String): String {
        val util = PhoneNumberUtil.getInstance()
        return util.format(util.parse(nationalNumber, regionCode), PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    override fun nationalNumberOf(e164: String): String? =
        e164.takeIf { it.startsWith(callingCode) }?.removePrefix(callingCode)?.takeIf(::isValid)

    override fun isValid(nationalNumber: String): Boolean {
        if (!nationalNumber.all(Char::isDigit)) return false
        val util = PhoneNumberUtil.getInstance()
        return try {
            val parsed = util.parse(nationalNumber, regionCode)
            util.isValidNumberForRegion(parsed, regionCode) && util.getNumberType(parsed) in mobileTypes
        } catch (ignored: NumberParseException) {
            false
        }
    }
}
