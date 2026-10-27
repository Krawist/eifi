package com.eifi.feature.onboarding

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eifi.core.data.AppMode
import kotlinx.serialization.Serializable

/** Navigation graph of the shared onboarding. */
@Serializable
data object OnboardingGraph

@Serializable
internal data object ModeChoiceRoute

/** Adds the onboarding graph; [onModeChosen] is called once the chosen mode is saved. */
fun NavGraphBuilder.onboardingGraph(onModeChosen: (AppMode) -> Unit) {
    navigation<OnboardingGraph>(startDestination = ModeChoiceRoute) {
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
            ModeChoiceScreen(onModeChosen = viewModel::chooseMode)
        }
    }
}
