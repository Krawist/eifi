package com.eifi.feature.pay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.eifi.feature.pay.receipt.ReceiptScreen
import com.eifi.feature.pay.receipt.ReceiptViewModel
import com.eifi.feature.pay.receipt.shareReceipt
import com.eifi.feature.pay.scan.ScanScreen
import com.eifi.feature.pay.summary.PaymentSummaryActions
import com.eifi.feature.pay.summary.PaymentSummaryScreen
import com.eifi.feature.pay.summary.PaymentSummaryViewModel
import kotlinx.coroutines.launch
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

/**
 * Receipt of a sent payment.
 *
 * @property paymentId id of the sent payment, see `ReceiptViewModel`.
 */
@Serializable
internal data class ReceiptRoute(val paymentId: String)

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
            PaymentSummaryDestination(
                onShowReceipt = { paymentId ->
                    navController.navigate(ReceiptRoute(paymentId)) {
                        popUpTo<PaymentSummaryRoute> { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }
        composable<ReceiptRoute> { entry ->
            ReceiptDestination(paymentId = entry.toRoute<ReceiptRoute>().paymentId, onDone = {
                navController.popBackStack()
            })
        }
    }
}

/** Opens the summary of the payment [url], read outside the Pay graph (home screen, App Link). */
fun NavController.navigateToPaymentSummary(url: String) {
    navigate(PaymentSummaryRoute(url)) { launchSingleTop = true }
}

@Composable
private fun PaymentSummaryDestination(onShowReceipt: (paymentId: String) -> Unit, onClose: () -> Unit) {
    val viewModel: PaymentSummaryViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
        null,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    uiState?.let {
        PaymentSummaryScreen(
            uiState = it,
            actions = PaymentSummaryActions(
                onPermissionDone = viewModel::onPermissionDone,
                onChangeAccount = viewModel::openAccountChoice,
                onChooseAccount = viewModel::chooseAccount,
                onCloseAccountChoice = viewModel::closeAccountChoice,
                onConfirm = viewModel::confirm,
                onAuthenticated = viewModel::onAuthenticated,
                onAuthenticationCancelled = viewModel::onAuthenticationCancelled,
                onScreenLockMissing = viewModel::onScreenLockMissing,
                onCloseScreenLockNeeded = viewModel::closeScreenLockNeeded,
                onShowReceipt = onShowReceipt,
                onClose = onClose,
            ),
        )
    }
}

/** The receipt; "Partager le reçu" shares the receipt part of the screen as an image. */
@Composable
private fun ReceiptDestination(paymentId: String, onDone: () -> Unit) {
    val viewModel: ReceiptViewModel = hiltViewModel(
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
        null,
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val receiptLayer = rememberGraphicsLayer()
    uiState?.let {
        ReceiptScreen(
            uiState = it,
            onDone = onDone,
            onShare = {
                scope.launch { shareReceipt(context, receiptLayer.toImageBitmap().asAndroidBitmap(), paymentId) }
            },
            receiptModifier = Modifier.drawWithContent {
                receiptLayer.record { this@drawWithContent.drawContent() }
                drawLayer(receiptLayer)
            },
        )
    }
}
