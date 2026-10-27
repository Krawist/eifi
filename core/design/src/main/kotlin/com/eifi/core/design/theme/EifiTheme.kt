package com.eifi.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EifiColorScheme = lightColorScheme(
    primary = EifiColors.Primary,
    onPrimary = EifiColors.OnPrimary,
    secondary = EifiColors.Accent,
    onSecondary = EifiColors.OnAccent,
    tertiary = EifiColors.Soft,
    onTertiary = EifiColors.OnSoft,
    background = EifiColors.Background,
    onBackground = EifiColors.Ink,
    surface = EifiColors.Surface,
    onSurface = EifiColors.Ink,
    onSurfaceVariant = EifiColors.Muted,
    outlineVariant = EifiColors.Divider,
    error = EifiColors.Error,
)

/** Eifi theme, plain and light version only for the pilot. */
@Composable
fun EifiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EifiColorScheme,
        typography = EifiTypography,
        shapes = EifiShapes,
        content = content,
    )
}
