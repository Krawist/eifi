package com.eifi.feature.onboarding

import com.eifi.core.data.AppMode
import com.eifi.core.data.UserPreferencesRepository
import io.kotest.matchers.nulls.shouldBeNull
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

internal class ModeChoiceViewModelTest {
    private val repository = object : UserPreferencesRepository {
        override val startMode = MutableStateFlow<AppMode?>(null)

        override suspend fun setStartMode(mode: AppMode) {
            startMode.value = mode
        }
    }

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
        fun `should select Pay at first`() {
            // when
            val viewModel = ModeChoiceViewModel(repository)

            // then
            viewModel.uiState.value.selectedMode shouldBe AppMode.Pay
        }

        @Test
        fun `should have no chosen mode at first`() {
            // when
            val viewModel = ModeChoiceViewModel(repository)

            // then
            viewModel.uiState.value.chosenMode.shouldBeNull()
        }
    }

    @Nested
    inner class Select {
        @Test
        fun `should select the mode`() {
            // given
            val viewModel = ModeChoiceViewModel(repository)

            // when
            viewModel.select(AppMode.Receive)

            // then
            viewModel.uiState.value.selectedMode shouldBe AppMode.Receive
        }

        @Test
        fun `should not save the mode before it is confirmed`() {
            // given
            val viewModel = ModeChoiceViewModel(repository)

            // when
            viewModel.select(AppMode.Receive)

            // then
            repository.startMode.value.shouldBeNull()
        }
    }

    @Nested
    inner class Confirm {
        @Test
        fun `should save the selected mode as the start mode`() {
            // given
            val viewModel = ModeChoiceViewModel(repository)
            viewModel.select(AppMode.Receive)

            // when
            viewModel.confirm()

            // then
            repository.startMode.value shouldBe AppMode.Receive
        }

        @Test
        fun `should expose the chosen mode once saved`() {
            // given
            val viewModel = ModeChoiceViewModel(repository)

            // when
            viewModel.confirm()

            // then
            viewModel.uiState.value.chosenMode shouldBe AppMode.Pay
        }
    }
}
