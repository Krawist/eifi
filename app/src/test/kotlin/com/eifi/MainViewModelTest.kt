package com.eifi

import com.eifi.core.data.AppMode
import com.eifi.core.data.UserPreferencesRepository
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

internal class MainViewModelTest {
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
        fun `should open on the onboarding when no mode was chosen`() {
            // when
            val viewModel = MainViewModel(repository)

            // then
            viewModel.uiState.value shouldBe MainUiState.Ready(startMode = null)
        }

        @Test
        fun `should open on the remembered mode`() {
            // given
            repository.startMode.value = AppMode.Receive

            // when
            val viewModel = MainViewModel(repository)

            // then
            viewModel.uiState.value shouldBe MainUiState.Ready(startMode = AppMode.Receive)
        }

        @Test
        fun `should keep the start destination when the mode changes later`() {
            // given
            val viewModel = MainViewModel(repository)

            // when
            repository.startMode.value = AppMode.Pay

            // then
            viewModel.uiState.value shouldBe MainUiState.Ready(startMode = null)
        }
    }
}
