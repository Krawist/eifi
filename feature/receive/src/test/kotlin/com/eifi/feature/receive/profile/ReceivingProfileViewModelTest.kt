package com.eifi.feature.receive.profile

import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.ReceivingProfileService
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import com.eifi.core.data.UserProfileService
import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import com.eifi.feature.receive.profile.phone.CameroonPhoneCountry
import com.eifi.feature.receive.profile.phone.PhoneCountry
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

internal class ReceivingProfileViewModelTest {
    private val receivingProfiles = object : ReceivingProfileRepository {
        override val profile = MutableStateFlow<ReceivingProfile?>(null)

        override suspend fun saveProfile(profile: ReceivingProfile) {
            this.profile.value = profile
        }
    }

    private val userPreferences = object : UserPreferencesRepository {
        override val profile =
            MutableStateFlow<UserProfile?>(UserProfile(personalName = "Paul", businessName = "Kiosque Ndé"))

        override suspend fun saveProfile(profile: UserProfile) = Unit
    }

    private val viewModel by lazy {
        ReceivingProfileViewModel(
            receivingProfileRepository = receivingProfiles,
            userPreferencesRepository = userPreferences,
            receivingProfileService = ReceivingProfileService(),
            userProfileService = UserProfileService(),
        )
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
        fun `should prefill the display name with the business name`() {
            // when
            val displayName = viewModel.uiState.value.displayName

            // then
            displayName shouldBe "Kiosque Ndé"
        }

        @Test
        fun `should show the saved profile when editing`() {
            // given
            receivingProfiles.profile.value = ReceivingProfile(
                displayName = "Boutique",
                receivingMethods = mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("730154")),
            )

            // when
            val state = viewModel.uiState.value

            // then
            state shouldBe ReceivingProfileUiState(
                phoneCountries = listOf(CameroonPhoneCountry),
                displayName = "Boutique",
                operators = mapOf(
                    Operator.MtnMomo to OperatorForm(CameroonPhoneCountry, ReceivingMethodKind.MerchantCode, "730154"),
                ),
            )
        }

        @Test
        fun `should offer Cameroon as the only phone country for now`() {
            // when
            val countries = viewModel.uiState.value.phoneCountries

            // then
            countries shouldBe listOf(CameroonPhoneCountry)
        }

        @Test
        fun `should select the first phone country by default`() {
            // when
            val country = viewModel.uiState.value.operators.getValue(Operator.OrangeMoney).country

            // then
            country shouldBe CameroonPhoneCountry
        }

        @Test
        fun `should not allow to save before a valid number is entered`() {
            // when
            val canSave = viewModel.uiState.value.canSave

            // then
            canSave.shouldBeFalse()
        }
    }

    @Nested
    inner class OnValueChange {
        @Test
        fun `should keep the national digits of a pasted phone number`() {
            // when
            viewModel.onValueChange(Operator.OrangeMoney, "+237 6 90 12 34 56")

            // then
            viewModel.uiState.value.operators.getValue(Operator.OrangeMoney).value shouldBe "690123456"
        }

        @Test
        fun `should allow to save with a valid number`() {
            // when
            viewModel.onValueChange(Operator.OrangeMoney, "690123456")

            // then
            viewModel.uiState.value.canSave.shouldBeTrue()
        }
    }

    @Nested
    inner class OnKindChange {
        @Test
        fun `should start the value again when switching to a merchant code`() {
            // given
            viewModel.onValueChange(Operator.OrangeMoney, "690123456")

            // when
            viewModel.onKindChange(Operator.OrangeMoney, ReceivingMethodKind.MerchantCode)

            // then
            viewModel.uiState.value.operators.getValue(Operator.OrangeMoney) shouldBe
                OperatorForm(country = CameroonPhoneCountry, kind = ReceivingMethodKind.MerchantCode)
        }
    }

    @Nested
    inner class OnSaved {
        @Test
        fun `should show a saved E164 number as a national number of its country`() {
            // given
            receivingProfiles.profile.value = ReceivingProfile(
                displayName = "Kiosque",
                receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.PhoneNumber("+237690123456")),
            )

            // when
            val form = viewModel.uiState.value.operators.getValue(Operator.OrangeMoney)

            // then
            form shouldBe OperatorForm(country = CameroonPhoneCountry, value = "690123456")
        }
    }

    @Nested
    inner class OnCountryChange {
        @Test
        fun `should start the number again for another country`() {
            // given
            val otherCountry = object : PhoneCountry by CameroonPhoneCountry {
                override val regionCode = "GA"
            }
            viewModel.onValueChange(Operator.OrangeMoney, "690123456")

            // when
            viewModel.onCountryChange(Operator.OrangeMoney, otherCountry)

            // then
            viewModel.uiState.value.operators.getValue(Operator.OrangeMoney) shouldBe
                OperatorForm(country = otherCountry)
        }
    }

    @Nested
    inner class AddOperator {
        @Test
        fun `should add the operator with an empty phone number`() {
            // when
            viewModel.addOperator(Operator.MtnMomo)

            // then
            viewModel.uiState.value.operators.getValue(Operator.MtnMomo) shouldBe
                OperatorForm(country = CameroonPhoneCountry)
        }
    }

    @Nested
    inner class RemoveOperator {
        @Test
        fun `should keep the last operator`() {
            // when
            viewModel.removeOperator(Operator.OrangeMoney)

            // then
            viewModel.uiState.value.operators.keys shouldBe setOf(Operator.OrangeMoney)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `should save the trimmed name and the receiving methods`() {
            // given
            viewModel.onDisplayNameChange(" Kiosque Ndé ")
            viewModel.onValueChange(Operator.OrangeMoney, "690123456")
            viewModel.addOperator(Operator.MtnMomo)
            viewModel.onKindChange(Operator.MtnMomo, ReceivingMethodKind.MerchantCode)
            viewModel.onValueChange(Operator.MtnMomo, "730154")

            // when
            viewModel.save()

            // then
            receivingProfiles.profile.value shouldBe ReceivingProfile(
                displayName = "Kiosque Ndé",
                receivingMethods = mapOf(
                    Operator.OrangeMoney to ReceivingMethod.PhoneNumber("+237690123456"),
                    Operator.MtnMomo to ReceivingMethod.MerchantCode("730154"),
                ),
            )
        }

        @Test
        fun `should show the errors when the profile is incomplete`() {
            // given
            viewModel.onDisplayNameChange("")

            // when
            viewModel.save()

            // then
            viewModel.uiState.value.showErrors.shouldBeTrue()
        }

        @Test
        fun `should save nothing while a value is invalid`() {
            // given
            viewModel.onValueChange(Operator.OrangeMoney, "6901")

            // when
            viewModel.save()

            // then
            receivingProfiles.profile.value.shouldBeNull()
        }
    }
}
