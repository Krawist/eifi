package com.eifi.feature.receive.profile

/** Why the value entered for an operator cannot be saved. */
internal enum class OperatorFormError {
    PhoneRequired,

    /** Shown as soon as it is typed; the others only once the user tried to save. */
    PhoneInvalid,

    MerchantCodeRequired,
}
