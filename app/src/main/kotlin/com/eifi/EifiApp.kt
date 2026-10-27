package com.eifi

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
import com.eifi.feature.onboarding.OnboardingGraph
import com.eifi.feature.onboarding.onboardingGraph
import com.eifi.feature.pay.PayGraph
import com.eifi.feature.pay.payGraph
import com.eifi.feature.receive.ReceiveGraph
import com.eifi.feature.receive.receiveGraph

/** Root of the app: one navigation graph per mode, opened on the remembered mode. */
@Composable
internal fun EifiApp(uiState: MainUiState) {
    when (uiState) {
        // Blank background while the preferences load (a few milliseconds).
        MainUiState.Loading -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        is MainUiState.Ready -> EifiNavHost(startMode = uiState.startMode)
    }
}

@Composable
private fun EifiNavHost(startMode: AppMode?, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = startMode?.graph ?: OnboardingGraph) {
        onboardingGraph(
            onModeChosen = { mode ->
                navController.navigate(mode.graph) {
                    popUpTo(OnboardingGraph) { inclusive = true }
                }
            },
        )
        payGraph(onBack = { navController.leaveMode(PayGraph) })
        receiveGraph(onBack = { navController.leaveMode(ReceiveGraph) })
    }
}

private val AppMode.graph: Any
    get() = when (this) {
        AppMode.Pay -> PayGraph
        AppMode.Receive -> ReceiveGraph
    }

/** Back to the mode choice until the home screen exists (EIFI-031). */
private fun NavHostController.leaveMode(graph: Any) {
    navigate(OnboardingGraph) {
        popUpTo(graph) { inclusive = true }
    }
}
