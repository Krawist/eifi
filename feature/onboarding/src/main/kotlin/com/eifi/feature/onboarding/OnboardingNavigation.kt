package com.eifi.feature.onboarding

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.eifi.core.data.AppMode
import kotlinx.serialization.Serializable

/** Navigation graph of the shared onboarding. */
@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

@Serializable
internal data object ModeChoiceRoute

/** [mode] is the mode chosen on the previous screen, opened once the names are saved. */
@Serializable
internal data class NamesRoute(val mode: AppMode)

/** Adds the onboarding graph; [onCompleted] is called once the profile is saved, with the chosen mode. */
fun NavGraphBuilder.onboardingGraph(navController: NavController, onCompleted: (AppMode) -> Unit) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            WelcomeScreen(onStart = { navController.navigate(ModeChoiceRoute) })
        }
        composable<ModeChoiceRoute> {
            val viewModel: ModeChoiceViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ModeChoiceScreen(
                uiState = uiState,
                onSelect = viewModel::select,
                onContinue = { navController.navigate(NamesRoute(uiState.selectedMode)) },
                onBack = navController.previousBackStackEntry?.let { { navController.popBackStack() } },
            )
        }
        composable<NamesRoute> { entry ->
            val mode = entry.toRoute<NamesRoute>().mode
            val viewModel: NamesViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.completed) {
                if (uiState.completed) onCompleted(mode)
            }
            NamesScreen(
                uiState = uiState,
                onPersonalNameChange = viewModel::onPersonalNameChange,
                onBusinessNameChange = viewModel::onBusinessNameChange,
                onFinish = viewModel::finish,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** Replays the whole onboarding, e.g. from the settings (EIFI-032). */
fun NavController.navigateToOnboarding(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(OnboardingGraph, builder)
}
