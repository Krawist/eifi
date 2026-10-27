package com.eifi.feature.receive.amount

/** User actions of [AmountEntryScreen], grouped to keep its parameter list short. */
internal data class AmountEntryActions(
    val onDigits: (String) -> Unit = {},
    val onDelete: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onShowLabel: () -> Unit = {},
    val onLabelChange: (String) -> Unit = {},
    val onCollect: () -> Unit = {},
    val onBack: () -> Unit = {},
)
