package com.eifi.feature.receive.profile

import com.eifi.core.payload.ReceivingMethod
import com.eifi.core.payload.ReceivingMethodService
import com.eifi.feature.receive.profile.phone.PhoneCountry

/**
 * What the user entered for one operator. Both values are kept while the user switches between them; only the one of
 * [kind] is saved.
 *
 * @property country country of the phone number, typed as a national number; unused for a merchant code.
 * @property kind phone number or merchant code: the value that is shown and saved.
 * @property phoneNumber the national phone number, as typed.
 * @property merchantCode the merchant code, as typed.
 */
internal data class OperatorForm(
    val country: PhoneCountry,
    val kind: ReceivingMethodKind = ReceivingMethodKind.PhoneNumber,
    val phoneNumber: String = "",
    val merchantCode: String = "",
) {
    /** The value of [kind], as typed. */
    val value: String
        get() = when (kind) {
            ReceivingMethodKind.PhoneNumber -> phoneNumber
            ReceivingMethodKind.MerchantCode -> merchantCode
        }

    /** The receiving method, or `null` while the value is not valid. */
    val receivingMethod: ReceivingMethod?
        get() = when (kind) {
            ReceivingMethodKind.PhoneNumber ->
                if (country.isValid(phoneNumber)) ReceivingMethod.PhoneNumber(country.toE164(phoneNumber)) else null
            ReceivingMethodKind.MerchantCode ->
                ReceivingMethod.MerchantCode(merchantCode.trim()).takeIf(ReceivingMethodService::isValid)
        }

    /** Why [receivingMethod] is `null`, or `null` when the value is valid. */
    val error: OperatorFormError?
        get() = when {
            receivingMethod != null -> null
            kind == ReceivingMethodKind.MerchantCode -> OperatorFormError.MerchantCodeRequired
            phoneNumber.isEmpty() -> OperatorFormError.PhoneRequired
            else -> OperatorFormError.PhoneInvalid
        }

    companion object {
        /** A saved E.164 number goes back to its country among [countries], as a national number. */
        fun of(method: ReceivingMethod, countries: List<PhoneCountry>): OperatorForm = when (method) {
            is ReceivingMethod.PhoneNumber -> countries.firstNotNullOfOrNull { country ->
                country.nationalNumberOf(method.value)?.let { OperatorForm(country = country, phoneNumber = it) }
            } ?: OperatorForm(country = countries.first())
            is ReceivingMethod.MerchantCode ->
                OperatorForm(
                    country = countries.first(),
                    kind = ReceivingMethodKind.MerchantCode,
                    merchantCode = method.value,
                )
        }
    }
}
