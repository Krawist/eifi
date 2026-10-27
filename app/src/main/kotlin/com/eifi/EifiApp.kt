package com.eifi

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.eifi.core.data.AppMode
import com.eifi.core.design.theme.EifiTransitions
import com.eifi.feature.onboarding.OnboardingGraph
import com.eifi.feature.onboarding.home.HomeGraph
import com.eifi.feature.onboarding.home.HomeGraphActions
import com.eifi.feature.onboarding.home.homeGraph
import com.eifi.feature.onboarding.onboardingGraph
import com.eifi.feature.pay.PayGraph
import com.eifi.feature.pay.navigateToPaymentSummary
import com.eifi.feature.pay.navigateToReceipt
import com.eifi.feature.pay.payGraph
import com.eifi.feature.receive.ReceiveGraph
import com.eifi.feature.receive.navigateToReceivingProfile
import com.eifi.feature.receive.receiveGraph

/**
 * Root of the app: the onboarding on first launch, then the home screen, which opens the two modes. [paymentLink]
 * opens its summary once the onboarding is done.
 */
@Composable
internal fun EifiApp(uiState: MainUiState, paymentLink: String?, onPaymentLinkOpened: () -> Unit) {
    AnimatedContent(targetState = uiState, transitionSpec = { EifiTransitions.Fade }, label = "app") { state ->
        when (state) {
            // Blank background while the preferences load (a few milliseconds).
            MainUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            )
            is MainUiState.Ready -> EifiNavHost(
                onboardingCompleted = state.onboardingCompleted,
                paymentLink = paymentLink,
                onPaymentLinkOpened = onPaymentLinkOpened,
            )
        }
    }
}

@Composable
private fun EifiNavHost(
    onboardingCompleted: Boolean,
    paymentLink: String?,
    onPaymentLinkOpened: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    LaunchedEffect(paymentLink) {
        if (paymentLink == null || !onboardingCompleted) return@LaunchedEffect
        onPaymentLinkOpened()
        navController.navigateToPaymentSummary(paymentLink)
    }
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
            actions = HomeGraphActions(
                onPay = { navController.navigate(PayGraph) },
                onPaymentUrl = { navController.navigateToPaymentSummary(it) },
                onReceive = { navController.navigate(ReceiveGraph) },
                onOpenReceipt = { navController.navigateToReceipt(it) },
                onEditReceivingProfile = { navController.navigateToReceivingProfile() },
            ),
        )
        payGraph(navController = navController, onBack = { navController.popBackStack() })
        receiveGraph(navController = navController, onBack = { navController.popBackStack() })
    }
}

private val AppMode.graph: Any
    get() = when (this) {
        AppMode.Pay -> PayGraph
        AppMode.Receive -> ReceiveGraph
    }
