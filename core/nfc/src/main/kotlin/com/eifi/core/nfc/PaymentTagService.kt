package com.eifi.core.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Answers the reader of a paying phone with the tag of [HceNfcPaymentEmitter] (AID D2760000850101). */
@AndroidEntryPoint
internal class PaymentTagService : HostApduService() {

    @Inject
    lateinit var emitter: HceNfcPaymentEmitter

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray =
        emitter.tag?.process(commandApdu) ?: Type4Tag.FILE_NOT_FOUND

    override fun onDeactivated(reason: Int) {
        emitter.tag?.reset()
    }
}
