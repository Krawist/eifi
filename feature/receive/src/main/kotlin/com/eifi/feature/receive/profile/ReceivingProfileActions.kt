package com.eifi.feature.receive.profile

import com.eifi.core.payload.Operator
import com.eifi.feature.receive.profile.phone.PhoneCountry

/** User actions of [ReceivingProfileScreen], grouped to keep its parameter list short. */
internal data class ReceivingProfileActions(
    val onDisplayNameChange: (String) -> Unit = {},
    val onKindChange: (Operator, ReceivingMethodKind) -> Unit = { _, _ -> },
    val onCountryChange: (Operator, PhoneCountry) -> Unit = { _, _ -> },
    val onValueChange: (Operator, String) -> Unit = { _, _ -> },
    val onAddOperator: (Operator) -> Unit = {},
    val onRemoveOperator: (Operator) -> Unit = {},
    val onSave: () -> Unit = {},
    val onBack: () -> Unit = {},
)
