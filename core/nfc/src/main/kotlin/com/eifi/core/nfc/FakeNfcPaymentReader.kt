package com.eifi.core.nfc

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Reads nothing. TODO(EIFI-200): reader mode with `NfcAdapter.enableReaderMode`. */
internal class FakeNfcPaymentReader @Inject constructor() : NfcPaymentReader {
    override val paymentUrls: Flow<String> = emptyFlow()
}
