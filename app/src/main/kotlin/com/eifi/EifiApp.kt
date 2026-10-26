package com.eifi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.eifi.feature.collect.CollectScreen
import com.eifi.feature.onboarding.Mode
import com.eifi.feature.onboarding.ModeChoiceScreen
import com.eifi.feature.pay.PayScreen

/**
 * Assemblage provisoire des modes (EIFI-010). La vraie navigation, avec
 * Navigation Compose et le mode mémorisé dans DataStore, arrive avec EIFI-012.
 */
@Composable
fun EifiApp() {
    var mode by rememberSaveable { mutableStateOf<Mode?>(null) }
    when (mode) {
        null -> ModeChoiceScreen(onModeChosen = { mode = it })
        Mode.Pay -> PayScreen(onBack = { mode = null })
        Mode.Collect -> CollectScreen(onBack = { mode = null })
    }
}
