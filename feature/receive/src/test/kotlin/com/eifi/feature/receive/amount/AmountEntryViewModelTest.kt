package com.eifi.feature.receive.amount

import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AmountEntryViewModelTest {
    private val repository = object : ReceivingProfileRepository {
        override val profile = MutableStateFlow<ReceivingProfile?>(
            ReceivingProfile(
                displayName = "Boutique Mama Ngono",
                receivingMethods = mapOf(
                    Operator.MtnMomo to ReceivingMethod.MerchantCode("730154"),
                    Operator.OrangeMoney to ReceivingMethod.PhoneNumber("+237690123456"),
                ),
            ),
        )

        override suspend fun saveProfile(profile: ReceivingProfile) = Unit
    }

    private val viewModel by lazy { AmountEntryViewModel(repository) }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should show the display name and the operators of the receiving profile`() {
            // when
            val state = viewModel.uiState.value

            // then
            state shouldBe AmountEntryUiState(
                displayName = "Boutique Mama Ngono",
                acceptedOperators = listOf(Operator.OrangeMoney, Operator.MtnMomo),
            )
        }

        @Test
        fun `should not allow to collect before an amount is typed`() {
            // when
            val canCollect = viewModel.uiState.value.canCollect

            // then
            canCollect.shouldBeFalse()
        }
    }

    @Nested
    inner class OnDigits {
        @Test
        fun `should type the amount digit after digit`() {
            // when
            listOf("2", "5", "000").forEach(viewModel::onDigits)

            // then
            viewModel.uiState.value.amount shouldBe 25_000
        }

        @Test
        fun `should allow to collect from 100 FCFA`() {
            // when
            listOf("1", "0", "0").forEach(viewModel::onDigits)

            // then
            viewModel.uiState.value.canCollect.shouldBeTrue()
        }
    }

    @Nested
    inner class Limits {
        @Test
        fun `should explain the minimum below 100 FCFA`() {
            // when
            listOf("5", "0").forEach(viewModel::onDigits)

            // then
            viewModel.uiState.value.belowMinimum.shouldBeTrue()
        }

        @Test
        fun `should explain the maximum when a key is ignored`() {
            // given
            listOf("2", "000", "000").forEach(viewModel::onDigits)

            // when
            viewModel.onDigits("0")

            // then
            viewModel.uiState.value.maximumReached.shouldBeTrue()
        }

        @Test
        fun `should stop explaining the maximum after a delete`() {
            // given
            listOf("2", "000", "000", "0").forEach(viewModel::onDigits)

            // when
            viewModel.onDelete()

            // then
            viewModel.uiState.value.maximumReached.shouldBeFalse()
        }
    }

    @Nested
    inner class OnDelete {
        @Test
        fun `should remove the last digit`() {
            // given
            listOf("2", "5").forEach(viewModel::onDigits)

            // when
            viewModel.onDelete()

            // then
            viewModel.uiState.value.amount shouldBe 2
        }
    }

    @Nested
    inner class OnLabelChange {
        @Test
        fun `should cut the label after 30 characters`() {
            // given
            viewModel.showLabel()

            // when
            viewModel.onLabelChange("L".repeat(40))

            // then
            viewModel.uiState.value.label shouldBe "L".repeat(30)
        }
    }
}
