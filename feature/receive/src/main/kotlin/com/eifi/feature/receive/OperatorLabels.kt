package com.eifi.feature.receive

import androidx.annotation.StringRes
import com.eifi.core.payload.Operator

/** Name of the operator, e.g. "Orange Money". */
@get:StringRes
internal val Operator.displayName: Int
    get() = when (this) {
        Operator.OrangeMoney -> R.string.operator_orange_money
        Operator.MtnMomo -> R.string.operator_mtn_momo
    }

/** Initials of the operator on its badge, e.g. "OM". */
@get:StringRes
internal val Operator.shortName: Int
    get() = when (this) {
        Operator.OrangeMoney -> R.string.operator_orange_money_short
        Operator.MtnMomo -> R.string.operator_mtn_momo_short
    }
