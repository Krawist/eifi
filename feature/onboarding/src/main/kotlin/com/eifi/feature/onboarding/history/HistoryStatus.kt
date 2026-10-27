package com.eifi.feature.onboarding.history

/** Status of a payment as the history shows it. */
internal enum class HistoryStatus {
    /** Received, still waiting for the payment. */
    Waiting,

    /** Received and confirmed by the operator's SMS. */
    Confirmed,

    /** Received and confirmed by the payee themselves ("Manuel" badge). */
    Manual,

    /** Received, expired before being paid. */
    Expired,

    /** Received, cancelled by the payee. */
    Cancelled,

    /** Sent: the USSD code was launched, the debit is not confirmed. */
    RequestSent,

    /** Sent and confirmed by the debit SMS (EIFI-206). */
    DebitConfirmed,
}
