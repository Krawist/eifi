package com.eifi.feature.receive

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

/** Navigation graph of the Receive mode. */
@Serializable
data object ReceiveGraph

@Serializable
internal data object ReceiveRoute

/** Adds the Receive graph; [onBack] leaves the mode. */
fun NavGraphBuilder.receiveGraph(onBack: () -> Unit) {
    navigation<ReceiveGraph>(startDestination = ReceiveRoute) {
        composable<ReceiveRoute> { ReceiveScreen(onBack = onBack) }
    }
}
