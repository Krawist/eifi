package com.eifi.feature.pay

/** What happens after a permission step of the Pay placeholder, until the real flows (EIFI-200, EIFI-204). */
internal enum class PayOutcome {
    /** Phone calls allowed: the USSD code will be launched. */
    UssdReady,

    /** Phone calls refused: the payer dials the USSD code shown. */
    DialYourself,

    /** Camera allowed: the scanner will open. */
    ScannerReady,

    /** Camera refused: the payer brings the phones close (NFC). */
    UseNfc,
}
