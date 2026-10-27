package com.eifi.feature.pay.summary

/** Where the confirmation of the payment stands. */
internal enum class ConfirmationStep {
    /** Not confirmed yet, or the biometric prompt was cancelled or failed. */
    None,

    /** The biometric prompt shows. */
    Authenticating,

    /** The phone has no screen lock: the payer must set one up before paying. */
    ScreenLockNeeded,

    /** The payer is authenticated: the USSD code is launched. */
    Authenticated,
}
