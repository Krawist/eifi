package com.eifi.feature.receive.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
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
            val greetingName = userPreferencesRepository.profile.first()?.greetingName.orEmpty()
            _uiState.update { state ->
                if (saved != null) {
                    state.copy(
                        displayName = saved.displayName,
                        operators = saved.receivingMethods.mapValues {
                            OperatorForm.of(it.value, PHONE_COUNTRIES)
                        },
                    )
                } else {
                    state.copy(displayName = greetingName.take(PaymentRequest.MAX_DISPLAY_NAME_LENGTH))
                }
            }
        }
    }

    fun onDisplayNameChange(name: String) {
        _uiState.update { it.copy(displayName = name.take(PaymentRequest.MAX_DISPLAY_NAME_LENGTH)) }
    }

    /** Switching between phone number and merchant code starts the value again. */
    fun onKindChange(operator: Operator, kind: ReceivingMethodKind) {
        updateOperator(operator) { if (it.kind == kind) it else OperatorForm(country = it.country, kind = kind) }
    }

    /** Another country starts the phone number again, as its rules differ. */
    fun onCountryChange(operator: Operator, country: PhoneCountry) {
        updateOperator(operator) { if (it.country == country) it else it.copy(country = country, value = "") }
    }

    /** A phone number keeps its national digits (see [PhoneCountry.normalize]); a merchant code is cut. */
    fun onValueChange(operator: Operator, value: String) {
        updateOperator(operator) { form ->
            val cleaned = when (form.kind) {
                ReceivingMethodKind.PhoneNumber -> form.country.normalize(value)
                ReceivingMethodKind.MerchantCode -> value.take(MAX_MERCHANT_CODE_LENGTH)
            }
            form.copy(value = cleaned)
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

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        val methods = state.operators.mapNotNull { (operator, form) ->
            form.receivingMethod?.let { operator to it }
        }.toMap()
        viewModelScope.launch {
            receivingProfileRepository.saveProfile(
                ReceivingProfile(displayName = state.displayName.trim(), receivingMethods = methods),
            )
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
        /** Same limit as [com.eifi.core.payload.ReceivingMethod.MerchantCode]. */
        const val MAX_MERCHANT_CODE_LENGTH = 32

        /** Countries a phone number can belong to; the first one is selected by default. */
        val PHONE_COUNTRIES: List<PhoneCountry> = listOf(CameroonPhoneCountry)
    }
}
