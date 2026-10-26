package com.eifi.core.design.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/**
 * Flat card of the plain theme: no elevation, no border, white on the light background.
 * Clickable when [onClick] is set; use [EifiColors.Primary] as [color] for the highlighted card of the home screen.
 */
@Composable
fun EifiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = EifiColors.Surface,
    shape: Shape = EifiRadius.Card,
    contentPadding: PaddingValues = PaddingValues(EifiSpacing.Gutter),
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
    if (onClick == null) {
        Surface(modifier = modifier, shape = shape, color = color, content = body)
    } else {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color, content = body)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiCardPreview() {
    EifiTheme {
        EifiCard(modifier = Modifier.fillMaxWidth().padding(EifiSpacing.Gutter), onClick = {}) {
            Text(text = "Encaisser", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Saisissez un montant, l'autre approche son téléphone",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
