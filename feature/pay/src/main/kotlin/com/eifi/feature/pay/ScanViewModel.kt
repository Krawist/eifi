package com.eifi.feature.pay

import androidx.lifecycle.ViewModel
import com.eifi.core.nfc.NfcPaymentReader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** The built-in scanner, which also reads NFC while it is open (EIFI-209). */
@HiltViewModel
internal class ScanViewModel @Inject constructor(nfcPaymentReader: NfcPaymentReader) : ViewModel() {
    /** Payment URLs read by NFC while the scanner is shown: each one opens its summary. */
    val paymentUrls: Flow<String> = nfcPaymentReader.paymentUrls
}
