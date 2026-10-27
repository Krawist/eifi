package com.eifi.feature.pay.summary

/** What launching the USSD code of a payment did. */
internal enum class LaunchOutcome {
    /** The code was called on the SIM of the account: the operator's prompt asks for the PIN. */
    Called,

    /** Without the phone calls permission, the dialer opened with the code typed. */
    DialerOpened,

    /** This session was already launched from this phone: it is never launched twice. */
    AlreadyLaunched,
}
