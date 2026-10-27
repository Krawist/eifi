package com.eifi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.eifi.feature.onboarding.Mode
import com.eifi.feature.onboarding.ModeChoiceScreen
import com.eifi.feature.pay.PayScreen
import com.eifi.feature.receive.ReceiveScreen

/**
 * Temporary assembly of the modes (EIFI-010). The real navigation, with
 * Navigation Compose and the mode remembered in DataStore, comes with EIFI-012.
 */
@Composable
fun EifiApp() {
    var mode by rememberSaveable { mutableStateOf<Mode?>(null) }
    when (mode) {
        null -> ModeChoiceScreen(onModeChosen = { mode = it })
        Mode.Pay -> PayScreen(onBack = { mode = null })
        Mode.Receive -> ReceiveScreen(onBack = { mode = null })
    }
}
