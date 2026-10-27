package com.eifi.feature.onboarding

import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
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

internal class NamesViewModelTest {
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
        fun `should prefill the saved names when the onboarding is replayed`() {
            // given
            repository.profile.value = UserProfile(personalName = "Paul", businessName = "Kiosque Ndé")

            // when
            val viewModel = NamesViewModel(repository)

            // then
            viewModel.uiState.value shouldBe NamesUiState(personalName = "Paul", businessName = "Kiosque Ndé")
        }

        @Test
        fun `should not allow to finish without any name`() {
            // when
            val viewModel = NamesViewModel(repository)

            // then
            viewModel.uiState.value.canFinish.shouldBeFalse()
        }
    }

    @Nested
    inner class OnBusinessNameChange {
        @Test
        fun `should allow to finish with the business name only`() {
            // given
            val viewModel = NamesViewModel(repository)

            // when
            viewModel.onBusinessNameChange("Kiosque Ndé")

            // then
            viewModel.uiState.value.canFinish.shouldBeTrue()
        }

        @Test
        fun `should cut a name after 30 characters`() {
            // given
            val viewModel = NamesViewModel(repository)

            // when
            viewModel.onBusinessNameChange("B".repeat(40))

            // then
            viewModel.uiState.value.businessName shouldBe "B".repeat(30)
        }
    }

    @Nested
    inner class Finish {
        @Test
        fun `should save the trimmed names, a blank one as missing`() {
            // given
            val viewModel = NamesViewModel(repository)
            viewModel.onPersonalNameChange("  Paul ")
            viewModel.onBusinessNameChange("   ")

            // when
            viewModel.finish()

            // then
            repository.profile.value shouldBe UserProfile(personalName = "Paul", businessName = null)
        }

        @Test
        fun `should complete the onboarding once the profile is saved`() {
            // given
            val viewModel = NamesViewModel(repository)
            viewModel.onPersonalNameChange("Paul")

            // when
            viewModel.finish()

            // then
            viewModel.uiState.value.completed.shouldBeTrue()
        }

        @Test
        fun `should show why it cannot finish without any name`() {
            // given
            val viewModel = NamesViewModel(repository)

            // when
            viewModel.finish()

            // then
            viewModel.uiState.value.showErrors.shouldBeTrue()
        }

        @Test
        fun `should save nothing without any name`() {
            // given
            val viewModel = NamesViewModel(repository)

            // when
            viewModel.finish()

            // then
            repository.profile.value.shouldBeNull()
        }
    }
}
