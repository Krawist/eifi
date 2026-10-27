package com.eifi.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Main call to action of a screen (« Encaisser 2 500 FCFA », « Payer »): full primary fill, dark ink text. */
@Composable
fun EifiPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = EifiRadius.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = EifiColors.Primary,
            contentColor = EifiColors.OnPrimary,
            disabledContainerColor = EifiColors.Divider,
            disabledContentColor = EifiColors.Muted,
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = EifiDimens.ButtonHeight),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Closing action filled with [EifiColors.Ink], white text (« Terminé » of the payer's receipt): keeps that screen
 * apart from the payee's ones, which use the primary colour.
 */
@Composable
fun EifiDarkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = EifiRadius.Button,
        colors = ButtonDefaults.buttonColors(containerColor = EifiColors.Ink, contentColor = EifiColors.Surface),
        modifier = modifier.fillMaxWidth().heightIn(min = EifiDimens.ButtonHeight),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Secondary action shown as a text link under the main button (« Voir l'historique du jour »). */
@Composable
fun EifiSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = EifiRadius.Button,
        colors = ButtonDefaults.textButtonColors(
            contentColor = EifiColors.OnPrimary,
            disabledContentColor = EifiColors.Muted,
        ),
        modifier = modifier.heightIn(min = EifiDimens.MinTouchTarget),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiButtonsPreview() {
    EifiTheme {
        Column(
            modifier = Modifier.padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        ) {
            EifiPrimaryButton(text = "Encaisser 2 500 FCFA", onClick = {})
            EifiPrimaryButton(text = "Encaisser", onClick = {}, enabled = false)
            EifiDarkButton(text = "Terminé", onClick = {})
            EifiSecondaryButton(text = "Voir l'historique du jour", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
