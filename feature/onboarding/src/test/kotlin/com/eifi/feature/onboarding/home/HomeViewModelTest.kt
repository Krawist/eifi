package com.eifi.feature.onboarding.home

import com.eifi.core.data.ActivityItem
import com.eifi.core.data.ActivityRepository
import com.eifi.core.data.PaymentDirection
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import com.eifi.core.data.UserProfileService
import com.eifi.core.nfc.NfcPaymentReader
import com.eifi.core.nfc.NfcStatus
import com.eifi.core.nfc.NfcStatusProvider
import com.eifi.core.payload.Operator
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModelTest {
    private val userPreferencesRepository = object : UserPreferencesRepository {
        override val profile = MutableStateFlow<UserProfile?>(null)

        override suspend fun saveProfile(profile: UserProfile) {
            this.profile.value = profile
        }
    }

    private val nfcStatus = MutableStateFlow(NfcStatus.Ready)

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
        fun `should greet with the business name and show the NFC status and activity`() = runTest {
            // given
            userPreferencesRepository.profile.value = UserProfile(personalName = "Paul", businessName = "Kiosque Ndé")
            nfcStatus.value = NfcStatus.Disabled
            val item = ActivityItem(
                id = "1",
                direction = PaymentDirection.Received,
                counterpart = "+237 6XX XX XX 41",
                operator = Operator.OrangeMoney,
                amount = 2_500,
                at = Instant.parse("2026-10-27T09:32:00Z"),
            )
            val viewModel = viewModel(activity = listOf(item))

            // when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

            // then
            viewModel.uiState.value shouldBe HomeUiState(
                greetingName = "Kiosque Ndé",
                nfcStatus = NfcStatus.Disabled,
                recentActivity = listOf(item),
            )
        }
    }

    private fun viewModel(activity: List<ActivityItem>) = HomeViewModel(
        userPreferencesRepository = userPreferencesRepository,
        activityRepository = object : ActivityRepository {
            override val recentActivities = flowOf(activity)
        },
        nfcStatusProvider = object : NfcStatusProvider {
            override val status = nfcStatus
        },
        nfcPaymentReader = object : NfcPaymentReader {
            override val paymentUrls = emptyFlow<String>()
        },
        userProfileService = UserProfileService(),
    )
}
