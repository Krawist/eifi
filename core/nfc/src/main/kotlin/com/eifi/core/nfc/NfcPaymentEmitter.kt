package com.eifi.core.nfc

/** Emits a payment URL as an NFC Forum Type 4 tag, so that a paying phone brought close reads it (EIFI-104). */
interface NfcPaymentEmitter {
    /** Emits [url] instead of any previous one. */
    fun start(url: String)

    /** Stops emitting [url]; does nothing when another URL has replaced it. */
    fun stop(url: String)
}
