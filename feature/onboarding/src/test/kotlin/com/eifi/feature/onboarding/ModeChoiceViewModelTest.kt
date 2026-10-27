package com.eifi.feature.onboarding

import com.eifi.core.data.AppMode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ModeChoiceViewModelTest {
    @Nested
    inner class UiState {
        @Test
        fun `should select Pay at first`() {
            // when
            val viewModel = ModeChoiceViewModel()

            // then
            viewModel.uiState.value.selectedMode shouldBe AppMode.Pay
        }
    }

    @Nested
    inner class Select {
        @Test
        fun `should select the mode`() {
            // given
            val viewModel = ModeChoiceViewModel()

            // when
            viewModel.select(AppMode.Receive)

            // then
            viewModel.uiState.value.selectedMode shouldBe AppMode.Receive
        }
    }
}
