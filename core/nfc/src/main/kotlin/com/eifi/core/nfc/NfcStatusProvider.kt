package com.eifi.core.nfc

import kotlinx.coroutines.flow.Flow

interface NfcStatusProvider {
    /** Current status, updated when the user turns NFC on or off. */
    val status: Flow<NfcStatus>
}
