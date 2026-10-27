package com.eifi.core.nfc

enum class NfcStatus {
    Ready,

    /** The phone has NFC but it is turned off in the settings. */
    Disabled,

    /** The phone has no NFC: the QR code is the only channel. */
    Unavailable,
}
