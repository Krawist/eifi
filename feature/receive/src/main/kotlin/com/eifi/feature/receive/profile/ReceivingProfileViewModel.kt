package com.eifi.feature.receive.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.ReceivingProfileService
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfileService
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequestService
import com.eifi.core.payload.ReceivingMethodService
import com.eifi.feature.receive.profile.phone.CameroonPhoneCountry
import com.eifi.feature.receive.profile.phone.PhoneCountry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * "Où recevoir l'argent": the saved profile when editing, otherwise the display name prefilled with the
 * business name (or the personal name) given in the onboarding, and Orange Money with a phone number.
 * A phone number starts with the first of the supported countries ([PHONE_COUNTRIES]).
 */
@HiltViewModel
internal class ReceivingProfileViewModel @Inject constructor(
    private val receivingProfileRepository: ReceivingProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val receivingProfileService: ReceivingProfileService,
    private val userProfileService: UserProfileService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReceivingProfileUiState(
            phoneCountries = PHONE_COUNTRIES,
            operators = mapOf(Operator.OrangeMoney to OperatorForm(country = PHONE_COUNTRIES.first())),
        ),
    )
    val uiState: StateFlow<ReceivingProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = receivingProfileRepository.profile.first()
            val greetingName = userPreferencesRepository.profile.first()?.let(
                userProfileService::greetingName,
            ).orEmpty()
            _uiState.update { state ->
                if (saved != null) {
                    state.copy(
                        displayName = saved.displayName,
                        operators = saved.receivingMethods.mapValues {
                            OperatorForm.of(it.value, PHONE_COUNTRIES)
                        },
                    )
                } else {
                    state.copy(displayName = greetingName.take(PaymentRequestService.MAX_DISPLAY_NAME_LENGTH))
                }
            }
        }
    }

    fun onDisplayNameChange(name: String) {
        _uiState.update { it.copy(displayName = name.take(PaymentRequestService.MAX_DISPLAY_NAME_LENGTH)) }
    }

    /** Switching between phone number and merchant code keeps what was typed in each. */
    fun onKindChange(operator: Operator, kind: ReceivingMethodKind) {
        updateOperator(operator) { it.copy(kind = kind) }
    }

    /** Another country starts the phone number again, as its rules differ. */
    fun onCountryChange(operator: Operator, country: PhoneCountry) {
        updateOperator(operator) { if (it.country == country) it else it.copy(country = country, phoneNumber = "") }
    }

    /** A phone number keeps its national digits (see [PhoneCountry.normalize]); a merchant code is cut. */
    fun onValueChange(operator: Operator, value: String) {
        updateOperator(operator) { form ->
            when (form.kind) {
                ReceivingMethodKind.PhoneNumber -> form.copy(phoneNumber = form.country.normalize(value))
                ReceivingMethodKind.MerchantCode ->
                    form.copy(merchantCode = value.take(ReceivingMethodService.MAX_MERCHANT_CODE_LENGTH))
            }
        }
    }

    fun addOperator(operator: Operator) {
        _uiState.update {
            it.copy(
                operators =
                it.operators + (operator to OperatorForm(country = PHONE_COUNTRIES.first())),
            )
        }
    }

    /** The last operator cannot be removed: the profile needs at least one. */
    fun removeOperator(operator: Operator) {
        _uiState.update { if (it.operators.size > 1) it.copy(operators = it.operators - operator) else it }
    }

    /** Saves the profile, or shows why it cannot be saved yet. */
    fun save() {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        val methods = state.operators.mapNotNull { (operator, form) ->
            form.receivingMethod?.let { operator to it }
        }.toMap()
        val profile = ReceivingProfile(displayName = state.displayName.trim(), receivingMethods = methods)
        receivingProfileService.validate(profile)
        viewModelScope.launch {
            receivingProfileRepository.saveProfile(profile)
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun updateOperator(operator: Operator, transform: (OperatorForm) -> OperatorForm) {
        _uiState.update { state ->
            val form = state.operators[operator] ?: return@update state
            state.copy(operators = state.operators + (operator to transform(form)))
        }
    }

    private companion object {
        /** Countries a phone number can belong to; the first one is selected by default. */
        val PHONE_COUNTRIES: List<PhoneCountry> = listOf(CameroonPhoneCountry)
    }
}
