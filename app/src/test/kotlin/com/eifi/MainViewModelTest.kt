package com.eifi

import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
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

internal class MainViewModelTest {
    private val repository = object : UserPreferencesRepository {
        override val profile = MutableStateFlow<UserProfile?>(null)

        override suspend fun saveProfile(profile: UserProfile) {
            this.profile.value = profile
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
        fun `should open on the onboarding without profile`() {
            // when
            val viewModel = MainViewModel(repository)

            // then
            viewModel.uiState.value shouldBe MainUiState.Ready(onboardingCompleted = false)
        }

        @Test
        fun `should open on the home screen once the profile is saved`() {
            // given
            repository.profile.value = UserProfile(personalName = "Paul", businessName = null)

            // when
            val viewModel = MainViewModel(repository)

            // then
            viewModel.uiState.value shouldBe MainUiState.Ready(onboardingCompleted = true)
        }

        @Test
        fun `should keep the start destination when the profile is saved later`() {
            // given
            val viewModel = MainViewModel(repository)

            // when
            repository.profile.value = UserProfile(personalName = "Paul", businessName = null)

            // then
            viewModel.uiState.value shouldBe MainUiState.Ready(onboardingCompleted = false)
        }
    }

    @Nested
    inner class PaymentLink {
        @Test
        fun `should keep the payment URL of a link until it is opened`() {
            // given
            val viewModel = MainViewModel(repository)

            // when
            viewModel.onPaymentLink("https://pay.eifi.app/p#v1.AAAA")

            // then
            viewModel.paymentLink.value shouldBe "https://pay.eifi.app/p#v1.AAAA"
        }

        @Test
        fun `should forget the payment URL once opened`() {
            // given
            val viewModel = MainViewModel(repository)
            viewModel.onPaymentLink("https://pay.eifi.app/p#v1.AAAA")

            // when
            viewModel.onPaymentLinkOpened()

            // then
            viewModel.paymentLink.value.shouldBeNull()
        }
    }
}
