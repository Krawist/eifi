package com.eifi.core.design.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.eifi.core.design.component.EifiPillTone
import kotlin.test.Test
import kotlin.test.assertTrue

class ContrastTest {
    @Test
    fun `every text colour pair reaches 4,5 to 1`() {
        // given
        val pairs = mapOf(
            "ink on background" to (EifiColors.Ink to EifiColors.Background),
            "ink on surface" to (EifiColors.Ink to EifiColors.Surface),
            "muted on background" to (EifiColors.Muted to EifiColors.Background),
            "muted on surface" to (EifiColors.Muted to EifiColors.Surface),
            "muted on divider (disabled button)" to (EifiColors.Muted to EifiColors.Divider),
            "on-primary on primary" to (EifiColors.OnPrimary to EifiColors.Primary),
            "on-primary on background (secondary button)" to (EifiColors.OnPrimary to EifiColors.Background),
        ) + EifiPillTone.entries.associate { "pill ${it.name}" to (it.content to it.container) }

        // when
        val failing = pairs.mapValues { (_, colors) -> contrast(colors.first, colors.second) }.filterValues { it < 4.5 }

        // then
        assertTrue(failing.isEmpty(), "Contrast below 4.5:1: $failing")
    }

    private fun contrast(foreground: Color, background: Color): Double {
        val lighter = maxOf(foreground.luminance(), background.luminance())
        val darker = minOf(foreground.luminance(), background.luminance())
        return (lighter + 0.05) / (darker + 0.05)
    }
}
