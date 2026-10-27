package com.eifi.feature.pay

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eifi.feature.pay.scan.ScanScreen
import com.eifi.feature.pay.summary.PaymentSummaryScreen
import com.eifi.feature.pay.summary.PaymentSummaryViewModel
import kotlinx.serialization.Serializable

/** Navigation graph of the Pay mode. */
@Serializable
data object PayGraph

@Serializable
internal data object PayRoute

/** Built-in QR scanner. */
@Serializable
internal data object ScanRoute

/**
 * Summary of a payment read by NFC, the scanner or an App Link.
 *
 * @property url the payment URL, decoded by the summary.
 */
@Serializable
internal data class PaymentSummaryRoute(val url: String)

/** Adds the Pay graph; [onBack] leaves the mode. */
fun NavGraphBuilder.payGraph(navController: NavController, onBack: () -> Unit) {
    navigation<PayGraph>(startDestination = PayRoute) {
        composable<PayRoute> {
            val viewModel: PayViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                viewModel.paymentUrls.collect { navController.navigateToPaymentSummary(it) }
            }
            LaunchedEffect(uiState.openScanner) {
                if (!uiState.openScanner) return@LaunchedEffect
                viewModel.onScannerOpened()
                navController.navigate(ScanRoute)
            }
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
        composable<ScanRoute> {
            ScanScreen(
                onScanned = { url ->
                    navController.navigate(PaymentSummaryRoute(url)) {
                        popUpTo<ScanRoute> { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }
        composable<PaymentSummaryRoute> {
            val viewModel: PaymentSummaryViewModel = hiltViewModel(
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
                null,
            )
            PaymentSummaryScreen(uiState = viewModel.uiState, onClose = { navController.popBackStack() })
        }
    }
}

/** Opens the summary of the payment [url], read outside the Pay graph (home screen, App Link). */
fun NavController.navigateToPaymentSummary(url: String) {
    navigate(PaymentSummaryRoute(url)) { launchSingleTop = true }
}
