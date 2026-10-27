package com.eifi.feature.onboarding.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eifi.feature.onboarding.history.HistoryScreen
import com.eifi.feature.onboarding.history.HistoryViewModel
import com.eifi.feature.onboarding.navigateToOnboarding
import com.eifi.feature.onboarding.settings.SettingsScreen
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

/** Adds the home graph; [actions] open what lies outside it. */
fun NavGraphBuilder.homeGraph(navController: NavController, actions: HomeGraphActions) {
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
                viewModel.paymentUrls.collect(actions.onPaymentUrl)
            }
            HomeScreen(
                uiState = uiState,
                onPay = actions.onPay,
                onReceive = actions.onReceive,
                onSeeAllActivity = { navController.navigate(ActivityHistoryRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<ActivityHistoryRoute> {
            val viewModel: HistoryViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            uiState?.let {
                HistoryScreen(uiState = it, onOpenReceipt = actions.onOpenReceipt, onBack = {
                    navController.popBackStack()
                })
            }
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onEditReceivingProfile = actions.onEditReceivingProfile,
                onReplayIntroduction = { navController.navigateToOnboarding() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
