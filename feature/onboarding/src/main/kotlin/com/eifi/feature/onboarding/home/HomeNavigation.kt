package com.eifi.feature.onboarding.home

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
import com.eifi.feature.onboarding.R
import com.eifi.feature.onboarding.navigateToOnboarding
import kotlinx.serialization.Serializable

/** Navigation graph of the home screen, with the screens it opens besides the modes. */
@Serializable
data object HomeGraph

@Serializable
internal data object HomeRoute

@Serializable
internal data object ActivityHistoryRoute

@Serializable
internal data object SettingsRoute

/** Adds the home graph; [onPay] and [onReceive] open the modes. */
fun NavGraphBuilder.homeGraph(navController: NavController, onPay: () -> Unit, onReceive: () -> Unit) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            val viewModel: HomeViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                // TODO(EIFI-200): pass the URL to the Pay flow.
                viewModel.paymentUrls.collect { onPay() }
            }
            HomeScreen(
                uiState = uiState,
                onPay = onPay,
                onReceive = onReceive,
                onSeeAllActivity = { navController.navigate(ActivityHistoryRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        // TODO(EIFI-109, EIFI-207): history of payments received and sent.
        composable<ActivityHistoryRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.history_title),
                subtitle = stringResource(R.string.history_subtitle),
                actionLabel = stringResource(R.string.history_back),
                onAction = { navController.popBackStack() },
            )
        }
        // TODO(EIFI-032): settings, with the permissions and the receiving profile.
        composable<SettingsRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                actionLabel = stringResource(R.string.settings_replay_introduction),
                onAction = { navController.navigateToOnboarding() },
            )
        }
    }
}
