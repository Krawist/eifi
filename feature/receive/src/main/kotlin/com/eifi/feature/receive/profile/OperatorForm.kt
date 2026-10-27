package com.eifi.feature.receive.profile

import com.eifi.core.payload.ReceivingMethod
import com.eifi.feature.receive.profile.phone.PhoneCountry

/** What the user entered for one operator; [country] applies to a phone number, typed as a national number. */
internal data class OperatorForm(
    val country: PhoneCountry,
    val kind: ReceivingMethodKind = ReceivingMethodKind.PhoneNumber,
    val value: String = "",
) {
    /** The receiving method, or `null` while the value is not valid. */
    val receivingMethod: ReceivingMethod?
        get() = when (kind) {
            ReceivingMethodKind.PhoneNumber ->
                if (country.isValid(value)) ReceivingMethod.PhoneNumber(country.toE164(value)) else null
            ReceivingMethodKind.MerchantCode -> runCatching { ReceivingMethod.MerchantCode(value.trim()) }.getOrNull()
        }

    companion object {
        /** A saved E.164 number goes back to its country among [countries], as a national number. */
        fun of(method: ReceivingMethod, countries: List<PhoneCountry>): OperatorForm = when (method) {
            is ReceivingMethod.PhoneNumber -> countries.firstNotNullOfOrNull { country ->
                country.nationalNumberOf(method.value)?.let { OperatorForm(country = country, value = it) }
            } ?: OperatorForm(country = countries.first())
            is ReceivingMethod.MerchantCode ->
                OperatorForm(country = countries.first(), kind = ReceivingMethodKind.MerchantCode, value = method.value)
        }
    }
}
