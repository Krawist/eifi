package com.eifi.core.nfc

import kotlinx.coroutines.flow.Flow

/** Reads the payment URL of a receiving phone brought close while a screen listens (reader mode). */
interface NfcPaymentReader {
    /** Payment URLs read from tags, as long as the flow is collected. */
    val paymentUrls: Flow<String>
}
