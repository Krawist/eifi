package com.eifi

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.eifi.core.data.AppMode
import com.eifi.core.design.theme.EifiTransitions
import com.eifi.feature.onboarding.OnboardingGraph
import com.eifi.feature.onboarding.home.HomeGraph
import com.eifi.feature.onboarding.home.homeGraph
import com.eifi.feature.onboarding.onboardingGraph
import com.eifi.feature.pay.PayGraph
import com.eifi.feature.pay.payGraph
import com.eifi.feature.receive.ReceiveGraph
import com.eifi.feature.receive.receiveGraph

/** Root of the app: the onboarding on first launch, then the home screen, which opens the two modes. */
@Composable
internal fun EifiApp(uiState: MainUiState) {
    AnimatedContent(targetState = uiState, transitionSpec = { EifiTransitions.Fade }, label = "app") { state ->
        when (state) {
            // Blank background while the preferences load (a few milliseconds).
            MainUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            )
            is MainUiState.Ready -> EifiNavHost(onboardingCompleted = state.onboardingCompleted)
        }
    }
}

@Composable
private fun EifiNavHost(onboardingCompleted: Boolean, navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = if (onboardingCompleted) HomeGraph else OnboardingGraph,
        enterTransition = { EifiTransitions.Enter },
        exitTransition = { EifiTransitions.Exit },
        popEnterTransition = { EifiTransitions.PopEnter },
        popExitTransition = { EifiTransitions.PopExit },
    ) {
        onboardingGraph(
            navController = navController,
            onCompleted = { mode ->
                // The chosen mode opens on top of the home screen, which replaces everything before it.
                navController.navigate(HomeGraph) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                navController.navigate(mode.graph)
            },
        )
        homeGraph(
            navController = navController,
            onPay = { navController.navigate(PayGraph) },
            onReceive = { navController.navigate(ReceiveGraph) },
        )
        payGraph(onBack = { navController.popBackStack() })
        receiveGraph(navController = navController, onBack = { navController.popBackStack() })
    }
}

private val AppMode.graph: Any
    get() = when (this) {
        AppMode.Pay -> PayGraph
        AppMode.Receive -> ReceiveGraph
    }
