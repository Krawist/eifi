package com.eifi.feature.receive.profile

import com.eifi.core.payload.Operator
import com.eifi.feature.receive.profile.phone.PhoneCountry

/**
 * [phoneCountries] are the countries a phone number can belong to. [showErrors] is set when the user tried to
 * save an incomplete profile, so that each missing value is explained. [saved] is set once the profile is
 * stored, so that the screen only navigates away afterwards.
 */
internal data class ReceivingProfileUiState(
    val phoneCountries: List<PhoneCountry>,
    val displayName: String = "",
    val operators: Map<Operator, OperatorForm> = emptyMap(),
    val showErrors: Boolean = false,
    val saved: Boolean = false,
) {
    val displayNameMissing: Boolean
        get() = displayName.isBlank()

    val canSave: Boolean
        get() = !displayNameMissing &&
            operators.isNotEmpty() &&
            operators.values.all { it.receivingMethod != null }

    /** Operators that can still be added, in a stable order. */
    val missingOperators: List<Operator>
        get() = Operator.entries.filterNot { it in operators }
}
