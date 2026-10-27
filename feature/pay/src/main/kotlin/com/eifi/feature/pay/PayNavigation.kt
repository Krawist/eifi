package com.eifi.feature.pay

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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
        composable<PayRoute> {
            val viewModel: PayViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            PayScreen(
                uiState = uiState,
                onPay = viewModel::onPay,
                onScan = viewModel::onScan,
                onPermissionDone = { granted ->
                    uiState.activePermission?.let { viewModel.onStepDone(it, granted) }
                },
                onBack = onBack,
            )
        }
    }
}
