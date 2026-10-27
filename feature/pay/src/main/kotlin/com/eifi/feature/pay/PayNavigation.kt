package com.eifi.feature.pay

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

/** Navigation graph of the Pay mode. */
@Serializable
data object PayGraph

@Serializable
internal data object PayRoute

/** Adds the Pay graph; [onBack] leaves the mode. */
fun NavGraphBuilder.payGraph(onBack: () -> Unit) {
    navigation<PayGraph>(startDestination = PayRoute) {
        composable<PayRoute> { PayScreen(onBack = onBack) }
    }
}
