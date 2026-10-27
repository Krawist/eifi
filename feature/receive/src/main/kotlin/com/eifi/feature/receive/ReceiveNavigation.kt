package com.eifi.feature.receive

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
import kotlinx.serialization.Serializable

/** Navigation graph of the Receive mode. */
@Serializable
data object ReceiveGraph

/** Opens the receiving profile first, until it is filled in (EIFI-100). */
@Serializable
internal data object ReceiveEntryRoute

@Serializable
internal data object ReceivingProfileRoute

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
                navController.navigate(if (configured) ReceiveRoute else ReceivingProfileRoute) {
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
                    navController.navigate(ReceiveRoute) {
                        popUpTo(ReceivingProfileRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<ReceiveRoute> { ReceiveScreen(onBack = onBack) }
    }
}
