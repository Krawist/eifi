package com.eifi.feature.pay.summary

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiCloseButton
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.design.theme.EifiTransitions
import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import com.eifi.core.permission.PermissionStep
import com.eifi.core.ussd.SimCard
import com.eifi.feature.pay.R
import com.eifi.feature.pay.displayName

/**
 * "Récapitulatif": the payee, the amount, the payer's account, the fees and the confirmation through the biometric
 * prompt, while the payee's code is valid, after the phone calls permission step the first time; "Choisissez votre
 * compte" on "Changer"; otherwise why the URL cannot be paid.
 */
@Composable
internal fun PaymentSummaryScreen(
    uiState: PaymentSummaryUiState,
    actions: PaymentSummaryActions,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PaymentSummaryUiState.Ready -> ReadyContent(uiState = uiState, actions = actions, modifier = modifier)
        PaymentSummaryUiState.Expired ->
            Problem(R.string.summary_expired_title, R.string.summary_expired_body, actions.onClose, modifier)
        PaymentSummaryUiState.UpdateNeeded ->
            Problem(R.string.summary_update_title, R.string.summary_update_body, actions.onClose, modifier)
        PaymentSummaryUiState.Invalid ->
            Problem(R.string.summary_invalid_title, R.string.summary_invalid_body, actions.onClose, modifier)
    }
}

/** The permission step, the screen lock explanation, "Choisissez votre compte" or the summary. */
@Composable
private fun ReadyContent(uiState: PaymentSummaryUiState.Ready, actions: PaymentSummaryActions, modifier: Modifier) {
    when {
        uiState.activePermission != null -> PermissionStep(
            permission = uiState.activePermission,
            onDone = actions.onPermissionDone,
            modifier = modifier,
            onBack = actions.onClose,
        )
        uiState.confirmationStep == ConfirmationStep.ScreenLockNeeded ->
            ScreenLockNeededScreen(onBack = actions.onCloseScreenLockNeeded, modifier = modifier)
        else -> AnimatedContent(
            targetState = uiState.choosingAccount,
            transitionSpec = { EifiTransitions.step(forward = targetState) },
            label = "account choice",
        ) { choosingAccount ->
            if (choosingAccount) {
                AccountChoiceScreen(
                    uiState = uiState,
                    onChoose = actions.onChooseAccount,
                    onBack = actions.onCloseAccountChoice,
                    modifier = modifier,
                )
            } else {
                Summary(uiState = uiState, actions = actions, modifier = modifier)
            }
        }
    }
    if (uiState.confirmationStep == ConfirmationStep.Authenticating) {
        BiometricConfirmation(
            title = stringResource(ReceivingMethodUi.of(uiState.receivingMethod).action, formatFcfa(uiState.amount)),
            subtitle = stringResource(R.string.summary_prompt_to, uiState.displayName),
            onAuthenticated = actions.onAuthenticated,
            onCancelled = actions.onAuthenticationCancelled,
            onScreenLockMissing = actions.onScreenLockMissing,
        )
    }
}

@Composable
private fun Summary(
    uiState: PaymentSummaryUiState.Ready,
    actions: PaymentSummaryActions,
    modifier: Modifier = Modifier,
) {
    val methodUi = ReceivingMethodUi.of(uiState.receivingMethod)
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            TopBar(
                title = stringResource(methodUi.title),
                remainingSeconds = uiState.remainingSeconds,
                onClose = actions.onClose,
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Payee(uiState = uiState, methodUi = methodUi)
                if (uiState.account == null) {
                    NoAccountCard(displayName = uiState.displayName, payeeOperators = uiState.payeeOperators)
                } else {
                    AccountDetails(
                        account = uiState.account,
                        methodUi = methodUi,
                        onChange = actions.onChangeAccount.takeIf { uiState.accounts.size > 1 },
                    )
                }
            }
            uiState.account?.let { BottomAction(uiState = uiState, account = it, onConfirm = actions.onConfirm) }
        }
    }
}

/**
 * The confirmation button; once launched, what the launch did; without known USSD code, why the payment is not
 * offered.
 */
@Composable
private fun BottomAction(uiState: PaymentSummaryUiState.Ready, account: PayingAccount, onConfirm: () -> Unit) {
    val operatorName = stringResource(account.operator.displayName)
    val message = when {
        !uiState.ussdAvailable -> stringResource(R.string.summary_unavailable, operatorName)
        uiState.launchOutcome == LaunchOutcome.Called -> stringResource(R.string.launch_called, operatorName)
        uiState.launchOutcome == LaunchOutcome.DialerOpened -> stringResource(R.string.launch_dialer_opened)
        uiState.launchOutcome == LaunchOutcome.AlreadyLaunched -> stringResource(R.string.launch_already_launched)
        else -> null
    }
    if (message == null) {
        EifiPrimaryButton(
            text = stringResource(ReceivingMethodUi.of(uiState.receivingMethod).action, formatFcfa(uiState.amount)),
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        EifiCard(
            modifier = Modifier.fillMaxWidth(),
            color = if (uiState.launchOutcome == LaunchOutcome.Called) EifiColors.Soft else EifiColors.AccentSoft,
            contentPadding = PaddingValues(EifiSpacing.Large),
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
    }
}

/** The account, the fees and the PIN reminder. */
@Composable
private fun AccountDetails(account: PayingAccount, methodUi: ReceivingMethodUi, onChange: (() -> Unit)?) {
    val operatorName = stringResource(account.operator.displayName)
    Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large)) {
        AccountCard(label = stringResource(methodUi.accountLabel), account = account, onChange = onChange)
        Fees(methodUi = methodUi, operatorName = operatorName)
        Note(icon = DesignR.drawable.ic_eifi_shield, text = stringResource(R.string.summary_pin_reminder, operatorName))
    }
}

@Composable
private fun TopBar(title: String, remainingSeconds: Long, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        EifiCloseButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        EifiPill(
            text = stringResource(
                R.string.summary_expires_in,
                "%d:%02d".format(remainingSeconds / SECONDS_PER_MINUTE, remainingSeconds % SECONDS_PER_MINUTE),
            ),
            modifier = Modifier.align(Alignment.CenterEnd),
            tone = EifiPillTone.Accent,
            icon = painterResource(DesignR.drawable.ic_eifi_timer),
        )
    }
}

@Composable
private fun Payee(uiState: PaymentSummaryUiState.Ready, methodUi: ReceivingMethodUi) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EifiIconTile(
            icon = painterResource(methodUi.icon),
            containerColor = methodUi.iconColor,
            size = EifiDimens.IconTileHero,
        )
        Text(
            text = uiState.displayName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        EifiPill(text = stringResource(methodUi.badge), tone = EifiPillTone.Neutral)
        AmountDisplay(amount = uiState.amount, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Problem(title: Int, body: Int, onClose: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(title),
        subtitle = stringResource(body),
        actionLabel = stringResource(R.string.summary_back),
        onAction = onClose,
        modifier = modifier,
    )
}

private const val SECONDS_PER_MINUTE = 60

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentSummaryScreenPreview() {
    val account = PayingAccount(
        operator = Operator.OrangeMoney,
        sim = SimCard(subscriptionId = 1, slotIndex = 0, carrierName = "Orange CM", operator = Operator.OrangeMoney),
    )
    EifiTheme {
        PaymentSummaryScreen(
            uiState = PaymentSummaryUiState.Ready(
                displayName = "Kiosque Ndé",
                amount = 2_500,
                payeeOperators = listOf(Operator.OrangeMoney, Operator.MtnMomo),
                accounts = listOf(account),
                account = account,
                receivingMethod = ReceivingMethod.MerchantCode("482913"),
                ussdAvailable = true,
                remainingSeconds = 102,
            ),
            actions = PaymentSummaryActions(),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentSummaryScreenNoAccountPreview() {
    EifiTheme {
        PaymentSummaryScreen(
            uiState = PaymentSummaryUiState.Ready(
                displayName = "Paul",
                amount = 2_500,
                payeeOperators = listOf(Operator.MtnMomo),
                accounts = emptyList(),
                account = null,
                receivingMethod = ReceivingMethod.PhoneNumber("+237670123456"),
                ussdAvailable = false,
                remainingSeconds = 102,
            ),
            actions = PaymentSummaryActions(),
        )
    }
}
