package com.eifi.feature.pay.summary

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.payload.ReceivingMethod
import com.eifi.feature.pay.R

/**
 * What the summary shows for a receiving method, so that the screen does not know each method.
 * TODO(EIFI-208): built from the embedded specification of the operator, with the fee amount.
 */
internal enum class ReceivingMethodUi {
    /** Merchant payment, free for the payer. */
    MerchantPayment,

    /** Person-to-person transfer: the payer pays the fees and the 0.2% tax. */
    Transfer,
    ;

    /** Title of the screen, "Payer" or "Envoyer". */
    @get:StringRes
    val title: Int
        get() = when (this) {
            MerchantPayment -> R.string.summary_merchant_title
            Transfer -> R.string.summary_transfer_title
        }

    /** Icon of the payee. */
    @get:DrawableRes
    val icon: Int
        get() = when (this) {
            MerchantPayment -> DesignR.drawable.ic_eifi_store
            Transfer -> DesignR.drawable.ic_eifi_person
        }

    /** Background of [icon]. */
    val iconColor: Color
        get() = when (this) {
            MerchantPayment -> EifiColors.Soft
            Transfer -> EifiColors.Accent
        }

    /** Kind of payee, under their name. */
    @get:StringRes
    val badge: Int
        get() = when (this) {
            MerchantPayment -> R.string.summary_merchant_badge
            Transfer -> R.string.summary_transfer_badge
        }

    /** Above the payer's account, "Payer avec" or "Envoyer avec". */
    @get:StringRes
    val accountLabel: Int
        get() = when (this) {
            MerchantPayment -> R.string.summary_merchant_with
            Transfer -> R.string.summary_transfer_with
        }

    /** Who pays the fees. */
    @get:StringRes
    val fees: Int
        get() = when (this) {
            MerchantPayment -> R.string.summary_merchant_fees
            Transfer -> R.string.summary_transfer_fees
        }

    /** What the fees are made of, with the operator name as argument; `null` without fees. */
    @get:StringRes
    val feesDetail: Int?
        get() = when (this) {
            MerchantPayment -> null
            Transfer -> R.string.summary_transfer_fees_detail
        }

    /** Background of the fee notice. */
    val feesColor: Color
        get() = when (this) {
            MerchantPayment -> EifiColors.Soft
            Transfer -> EifiColors.AccentSoft
        }

    /** Label of the confirmation button, with the amount as argument. */
    @get:StringRes
    val action: Int
        get() = when (this) {
            MerchantPayment -> R.string.summary_merchant_action
            Transfer -> R.string.summary_transfer_action
        }

    companion object {
        fun of(method: ReceivingMethod): ReceivingMethodUi = when (method) {
            is ReceivingMethod.MerchantCode -> MerchantPayment
            is ReceivingMethod.PhoneNumber -> Transfer
        }
    }
}
