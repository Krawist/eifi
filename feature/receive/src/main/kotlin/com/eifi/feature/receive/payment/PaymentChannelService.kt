package com.eifi.feature.receive.payment

import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.nfc.NfcPaymentEmitter
import com.eifi.core.nfc.NfcStatus
import com.eifi.core.nfc.NfcStatusProvider
import com.eifi.core.payload.PaymentUrlCodec
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The two channels a received payment is offered on: its URL, shown as a QR code, and the same URL over NFC. */
internal class PaymentChannelService @Inject constructor(
    private val receivingProfileRepository: ReceivingProfileRepository,
    private val paymentService: PaymentService,
    private val paymentUrlCodec: PaymentUrlCodec,
    private val nfcPaymentEmitter: NfcPaymentEmitter,
    nfcStatusProvider: NfcStatusProvider,
) {
    /** Whether NFC can carry the URL; the QR code works in any case. */
    val nfcStatus: Flow<NfcStatus> = nfcStatusProvider.status

    /** Payment URL of [payment], paid to the receiving profile; `null` while there is no profile. */
    fun url(payment: Payment): Flow<String?> = receivingProfileRepository.profile.map { profile ->
        profile?.let { paymentUrlCodec.encode(paymentService.toPaymentRequest(payment = payment, profile = it)) }
    }

    /** Emits [url] over NFC until [stopEmitting]. */
    fun emit(url: String) = nfcPaymentEmitter.start(url)

    fun stopEmitting(url: String) = nfcPaymentEmitter.stop(url)
}
