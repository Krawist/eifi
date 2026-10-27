package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.ReceivingMethod
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PaymentSummaryViewModelTest {
    private val now = Instant.parse("2026-10-27T10:00:00Z")

    private val codec = PaymentUrlCodec(baseUrl = "https://pay.eifi.app", clock = Clock.fixed(now, ZoneOffset.UTC))

    private fun viewModel(url: String) =
        PaymentSummaryViewModel(SavedStateHandle(mapOf(PaymentSummaryViewModel.PAYMENT_URL to url)), codec)

    /** URL of a payment of 2 500 FCFA to "Kiosque Ndé", expiring at [expiresAt]. */
    private fun url(expiresAt: Instant) = codec.encode(
        PaymentRequest(
            id = "0a1b2c3d4e5f",
            displayName = "Kiosque Ndé",
            amount = 2_500,
            receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
            expiresAt = expiresAt,
        ),
    )

    @Nested
    inner class UiState {
        @Test
        fun `should show the payee and the amount of a valid payment`() {
            // when
            val viewModel = viewModel(url(expiresAt = now.plusSeconds(60)))

            // then
            viewModel.uiState shouldBe PaymentSummaryUiState.Ready(displayName = "Kiosque Ndé", amount = 2_500)
        }

        @Test
        fun `should say that an expired payment can no longer be paid`() {
            // when
            val viewModel = viewModel(url(expiresAt = now))

            // then
            viewModel.uiState shouldBe PaymentSummaryUiState.Expired
        }

        @Test
        fun `should ask to update the app for a newer URL version`() {
            // when
            val viewModel = viewModel("https://pay.eifi.app/p#v2.AAAA")

            // then
            viewModel.uiState shouldBe PaymentSummaryUiState.UpdateNeeded
        }

        @Test
        fun `should reject a URL that is not an Eifi payment`() {
            // when
            val viewModel = viewModel("https://example.com/p#v1.AAAA")

            // then
            viewModel.uiState shouldBe PaymentSummaryUiState.Invalid
        }
    }
}
