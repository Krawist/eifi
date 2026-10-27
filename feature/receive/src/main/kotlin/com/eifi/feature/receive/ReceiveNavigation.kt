package com.eifi.feature.receive

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.eifi.core.data.AppPermission
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiTransitions
import com.eifi.core.permission.PermissionStep
import com.eifi.core.permission.rememberPermissionGranted
import com.eifi.feature.receive.amount.AmountEntryActions
import com.eifi.feature.receive.amount.AmountEntryScreen
import com.eifi.feature.receive.amount.AmountEntryViewModel
import com.eifi.feature.receive.profile.ReceivingProfileActions
import com.eifi.feature.receive.profile.ReceivingProfileScreen
import com.eifi.feature.receive.profile.ReceivingProfileViewModel
import kotlinx.serialization.Serializable

/** Navigation graph of the Receive mode. */
@Serializable
data object ReceiveGraph

/** Opens the receiving profile first, until it is filled in (EIFI-100). */
@Serializable
internal data object ReceiveEntryRoute

/**
 * "Où recevoir l'argent".
 *
 * @property editing whether it was opened from the settings, which it goes back to once saved.
 */
@Serializable
internal data class ReceivingProfileRoute(val editing: Boolean = false)

/** Notification access and battery optimisation, asked the first time the mode is used (EIFI-032). */
@Serializable
internal data object ReceivePermissionsRoute

/** "Saisie du montant", the main screen of the mode. */
@Serializable
internal data object ReceiveRoute

/** Payment session of [amount]: placeholder of the "QR et NFC actifs" screen until EIFI-102 and EIFI-103. */
@Serializable
internal data class SessionPlaceholderRoute(val amount: Long, val label: String? = null)

/** Adds the Receive graph; [onBack] leaves the mode. */
fun NavGraphBuilder.receiveGraph(navController: NavController, onBack: () -> Unit) {
    navigation<ReceiveGraph>(startDestination = ReceiveEntryRoute) {
        composable<ReceiveEntryRoute> {
            val viewModel: ReceiveEntryViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val profileConfigured by viewModel.profileConfigured.collectAsStateWithLifecycle()
            LaunchedEffect(profileConfigured) {
                val configured = profileConfigured ?: return@LaunchedEffect
                navController.navigate(if (configured) ReceivePermissionsRoute else ReceivingProfileRoute()) {
                    popUpTo(ReceiveEntryRoute) { inclusive = true }
                }
            }
        }
        composable<ReceivingProfileRoute> { entry ->
            ReceivingProfileDestination(
                navController = navController,
                editing = entry.toRoute<ReceivingProfileRoute>().editing,
                onBack = onBack,
            )
        }
        composable<ReceivePermissionsRoute> {
            ReceivePermissionsDestination(navController = navController, onBack = onBack)
        }
        composable<ReceiveRoute> {
            AmountEntryDestination(navController = navController, onBack = onBack)
        }
        // TODO(EIFI-102, EIFI-103): create the session, then show its QR code and emit it over NFC.
        composable<SessionPlaceholderRoute> { entry ->
            val session = entry.toRoute<SessionPlaceholderRoute>()
            PlaceholderScreen(
                title = stringResource(R.string.session_placeholder_title),
                subtitle = stringResource(R.string.session_placeholder_subtitle, formatFcfa(session.amount)),
                actionLabel = stringResource(R.string.session_placeholder_back),
                onAction = { navController.popBackStack() },
            )
        }
    }
}

/** Notification access, then battery, sliding from one step to the next. */
@Composable
private fun ReceivePermissionsDestination(navController: NavController, onBack: () -> Unit) {
    val viewModel: ReceivePermissionsViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
        null,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        ReceivePermissionsUiState.Loading -> Unit
        is ReceivePermissionsUiState.Asking -> AnimatedContent(
            targetState = state.permission,
            transitionSpec = { EifiTransitions.step(forward = true) },
            label = "receive permissions",
        ) { permission ->
            PermissionStep(
                permission = permission,
                onDone = { granted -> viewModel.onStepDone(permission, granted) },
                onBack = onBack,
            )
        }
        ReceivePermissionsUiState.Done -> LaunchedEffect(Unit) {
            navController.navigate(ReceiveRoute) {
                popUpTo(ReceivePermissionsRoute) { inclusive = true }
            }
        }
    }
}

/** Edits the receiving profile from the settings. */
fun NavController.navigateToReceivingProfile() {
    navigate(ReceivingProfileRoute(editing = true))
}

/** First use: the permissions follow once saved. Editing: back to the settings once saved. */
@Composable
private fun ReceivingProfileDestination(navController: NavController, editing: Boolean, onBack: () -> Unit) {
    val viewModel: ReceivingProfileViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
        null,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val leave: () -> Unit = if (editing) ({ navController.popBackStack() }) else onBack
    LaunchedEffect(uiState.saved) {
        when {
            !uiState.saved -> Unit
            editing -> navController.popBackStack()
            else -> navController.navigate(ReceivePermissionsRoute) {
                popUpTo<ReceivingProfileRoute> { inclusive = true }
            }
        }
    }
    ReceivingProfileScreen(
        uiState = uiState,
        editing = editing,
        actions = ReceivingProfileActions(
            onDisplayNameChange = viewModel::onDisplayNameChange,
            onKindChange = viewModel::onKindChange,
            onCountryChange = viewModel::onCountryChange,
            onValueChange = viewModel::onValueChange,
            onAddOperator = viewModel::addOperator,
            onRemoveOperator = viewModel::removeOperator,
            onSave = viewModel::save,
            onBack = leave,
        ),
    )
}

@Composable
private fun AmountEntryDestination(navController: NavController, onBack: () -> Unit) {
    val viewModel: AmountEntryViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
        null,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val automaticConfirmation by rememberPermissionGranted(AppPermission.NotificationAccess)
    AmountEntryScreen(
        uiState = uiState,
        automaticConfirmation = automaticConfirmation,
        actions = AmountEntryActions(
            onDigits = viewModel::onDigits,
            onDelete = viewModel::onDelete,
            onShowLabel = viewModel::showLabel,
            onLabelChange = viewModel::onLabelChange,
            onCollect = {
                navController.navigate(
                    SessionPlaceholderRoute(
                        amount = uiState.amount,
                        label = uiState.label.ifBlank {
                            null
                        },
                    ),
                )
            },
            onBack = onBack,
        ),
    )
}
