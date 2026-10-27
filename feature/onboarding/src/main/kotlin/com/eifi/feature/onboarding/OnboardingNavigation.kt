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
import com.eifi.core.data.AppMode
import kotlinx.serialization.Serializable

/** Navigation graph of the shared onboarding. */
@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

@Serializable
internal data object ModeChoiceRoute

/** Adds the onboarding graph; [onModeChosen] is called once the chosen mode is saved. */
fun NavGraphBuilder.onboardingGraph(navController: NavController, onModeChosen: (AppMode) -> Unit) {
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
            LaunchedEffect(uiState.chosenMode) {
                uiState.chosenMode?.let(onModeChosen)
            }
            ModeChoiceScreen(
                uiState = uiState,
                onSelect = viewModel::select,
                onContinue = viewModel::confirm,
                onBack = navController.previousBackStackEntry?.let { { navController.popBackStack() } },
            )
        }
    }
}

/** Opens the mode choice without the welcome screen, e.g. when leaving a mode. */
fun NavController.navigateToModeChoice(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(ModeChoiceRoute, builder)
}

/** Replays the whole onboarding, e.g. from the settings (EIFI-032). */
fun NavController.navigateToOnboarding(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(OnboardingGraph, builder)
}
