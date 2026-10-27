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
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.design.theme.EifiTransitions
import com.eifi.core.permission.PermissionStep
import kotlinx.serialization.Serializable

/** Navigation graph of the Receive mode. */
@Serializable
data object ReceiveGraph

/** Opens the receiving profile first, until it is filled in (EIFI-100). */
@Serializable
internal data object ReceiveEntryRoute

@Serializable
internal data object ReceivingProfileRoute

/** Notification access and battery optimisation, asked the first time the mode is used (EIFI-032). */
@Serializable
internal data object ReceivePermissionsRoute

@Serializable
internal data object ReceiveRoute

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
                navController.navigate(if (configured) ReceivePermissionsRoute else ReceivingProfileRoute) {
                    popUpTo(ReceiveEntryRoute) { inclusive = true }
                }
            }
        }
        // TODO(EIFI-100): receiving profile, then notification access and battery optimisation.
        composable<ReceivingProfileRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.receiving_profile_title),
                subtitle = stringResource(R.string.receiving_profile_subtitle),
                actionLabel = stringResource(R.string.receiving_profile_continue),
                onAction = {
                    navController.navigate(ReceivePermissionsRoute) {
                        popUpTo(ReceivingProfileRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<ReceivePermissionsRoute> {
            ReceivePermissionsDestination(navController = navController, onBack = onBack)
        }
        composable<ReceiveRoute> { ReceiveScreen(onBack = onBack) }
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
