package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.PaymentUrlDecodingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Decodes the payment URL read by NFC, the scanner or an App Link, once, when the summary opens. */
@HiltViewModel
internal class PaymentSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    paymentUrlCodec: PaymentUrlCodec,
) : ViewModel() {

    val uiState: PaymentSummaryUiState =
        when (val result = paymentUrlCodec.decode(checkNotNull(savedStateHandle[PAYMENT_URL]) { "No payment URL" })) {
            is PaymentUrlDecodingResult.Valid ->
                PaymentSummaryUiState.Ready(displayName = result.request.displayName, amount = result.request.amount)
            is PaymentUrlDecodingResult.Expired -> PaymentSummaryUiState.Expired
            is PaymentUrlDecodingResult.UnknownVersion -> PaymentSummaryUiState.UpdateNeeded
            is PaymentUrlDecodingResult.Malformed -> PaymentSummaryUiState.Invalid
        }

    companion object {
        /** Name of the route argument, see `PaymentSummaryRoute`. */
        const val PAYMENT_URL = "url"
    }
}
