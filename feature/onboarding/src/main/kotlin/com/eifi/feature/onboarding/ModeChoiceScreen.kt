package com.eifi.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiTheme

enum class Mode { Pay, Collect }

@Composable
fun ModeChoiceScreen(onModeChosen: (Mode) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Button(
                onClick = { onModeChosen(Mode.Pay) },
                shape = EifiRadius.Button,
                modifier = Modifier.fillMaxWidth().height(58.dp),
            ) {
                Text(stringResource(R.string.mode_pay), style = MaterialTheme.typography.labelLarge)
            }
            FilledTonalButton(
                onClick = { onModeChosen(Mode.Collect) },
                shape = EifiRadius.Button,
                modifier = Modifier.fillMaxWidth().height(58.dp),
            ) {
                Text(stringResource(R.string.mode_collect), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview
@Composable
private fun ModeChoiceScreenPreview() {
    EifiTheme { ModeChoiceScreen(onModeChosen = {}) }
}
