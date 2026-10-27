package com.eifi.core.design.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eifi.core.design.R
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.component.NumericKeypad
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Preview-only catalogue of the design system: colours, type scale and every shared component. */
@Preview(name = "Catalogue", showBackground = true, backgroundColor = 0xFFF6F8F7, widthDp = 390, heightDp = 2200)
@Composable
private fun DesignSystemCatalogPreview() {
    EifiTheme {
        Column(
            modifier = Modifier
                .background(EifiColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.XLarge),
        ) {
            Section("Couleurs") { Palette() }
            Section("Typographie") { TypeScale() }
            Section("Montant") { AmountDisplay(amount = 2_500, modifier = Modifier.fillMaxWidth()) }
            Section("Boutons") {
                EifiPrimaryButton(text = "Payer 2 500 FCFA", onClick = {})
                EifiPrimaryButton(text = "Encaisser", onClick = {}, enabled = false)
                EifiSecondaryButton(text = "Voir l'historique du jour", onClick = {
                }, modifier = Modifier.fillMaxWidth())
            }
            Section("Pastilles") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small)) {
                    EifiPill(text = "Orange Money", icon = painterResource(R.drawable.ic_eifi_check))
                    EifiPill(text = "MTN MoMo", icon = painterResource(R.drawable.ic_eifi_check))
                    EifiPill(text = "Manuel", tone = EifiPillTone.Accent)
                    EifiPill(text = "SIM 1", tone = EifiPillTone.Neutral)
                }
            }
            Section("Cartes") {
                EifiCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                    Text(text = "Encaisser", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Saisissez un montant, l'autre approche son téléphone",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                EifiCard(modifier = Modifier.fillMaxWidth(), color = EifiColors.Primary, shape = EifiRadius.CardLarge) {
                    Text(text = "Payer", style = MaterialTheme.typography.headlineLarge, color = EifiColors.OnPrimary)
                    Text(
                        text = "Approchez un téléphone Eifi ou scannez son code",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EifiColors.OnPrimary,
                    )
                }
            }
            Section("Pavé numérique") { NumericKeypad(onDigits = {}, onDelete = {}, onClear = {}) }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium)) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = EifiColors.Muted)
        content()
    }
}

@Composable
private fun Palette() {
    val swatches = listOf(
        "primary" to EifiColors.Primary,
        "on-primary" to EifiColors.OnPrimary,
        "accent" to EifiColors.Accent,
        "soft" to EifiColors.Soft,
        "background" to EifiColors.Background,
        "surface" to EifiColors.Surface,
        "ink" to EifiColors.Ink,
        "muted" to EifiColors.Muted,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
    ) {
        swatches.forEach { (name, color) -> Swatch(name = name, color = color) }
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(72.dp).background(color, EifiRadius.Button))
        Text(text = name, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TypeScale() {
    val typography = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.XSmall)) {
        Text(text = "Headline 34/800", style = typography.headlineLarge)
        Text(text = "Headline 28/800", style = typography.headlineMedium)
        Text(text = "Title 20/700", style = typography.titleLarge)
        Text(text = "Title 17/700", style = typography.titleMedium)
        Text(text = "Body 15/500", style = typography.bodyLarge)
        Text(text = "Body 14/500", style = typography.bodyMedium)
        Text(text = "Label 13/600", style = typography.labelMedium)
    }
}
