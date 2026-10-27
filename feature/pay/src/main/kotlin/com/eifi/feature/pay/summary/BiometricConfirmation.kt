package com.eifi.feature.pay.summary

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Shows the mandatory biometric prompt: fingerprint, face or the phone unlock code. Calls [onScreenLockMissing]
 * when the phone has no screen lock, [onCancelled] when the payer cancels or the prompt fails; leaving the
 * composition cancels the prompt.
 */
@Composable
internal fun BiometricConfirmation(
    title: String,
    subtitle: String,
    onAuthenticated: () -> Unit,
    onCancelled: () -> Unit,
    onScreenLockMissing: () -> Unit,
) {
    val activity = LocalActivity.current as? FragmentActivity
    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)
    val currentOnCancelled by rememberUpdatedState(onCancelled)
    val currentOnScreenLockMissing by rememberUpdatedState(onScreenLockMissing)
    DisposableEffect(activity) {
        val prompt = activity?.takeIf {
            BiometricManager.from(it).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
        }?.let {
            BiometricPrompt(
                it,
                ContextCompat.getMainExecutor(it),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        currentOnAuthenticated()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        currentOnCancelled()
                    }
                },
            )
        }
        if (prompt == null) {
            currentOnScreenLockMissing()
        } else {
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(AUTHENTICATORS)
                    .build(),
            )
        }
        onDispose { prompt?.cancelAuthentication() }
    }
}

/**
 * Strong biometrics or the unlock code; before Android 11, the unlock code can only be combined with any
 * biometrics.
 */
private val AUTHENTICATORS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
} else {
    BIOMETRIC_WEAK or DEVICE_CREDENTIAL
}
