package com.eifi.core.design.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.eifi.core.design.component.EifiPillTone
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

internal class ContrastTest {
    /** Colours are passed as ARGB integers, as JUnit cannot inject Compose's inline `Color` class. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("textColourPairs")
    fun `should reach a contrast of 4,5 to 1 for every text colour pair`(foreground: Int, background: Int) {
        // when
        val contrast = contrast(Color(foreground), Color(background))

        // then
        contrast shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
    }

    @ParameterizedTest
    @EnumSource(EifiPillTone::class)
    fun `should reach a contrast of 4,5 to 1 for every pill tone`(tone: EifiPillTone) {
        // when
        val contrast = contrast(tone.content, tone.container)

        // then
        contrast shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
    }

    private fun contrast(foreground: Color, background: Color): Double {
        val lighter = maxOf(foreground.luminance(), background.luminance())
        val darker = minOf(foreground.luminance(), background.luminance())
        return (lighter + 0.05) / (darker + 0.05)
    }

    private companion object {
        const val MIN_TEXT_CONTRAST = 4.5

        @JvmStatic
        fun textColourPairs() = listOf(
            pair("ink on background", EifiColors.Ink, EifiColors.Background),
            pair("ink on surface", EifiColors.Ink, EifiColors.Surface),
            pair("surface on ink (dark button)", EifiColors.Surface, EifiColors.Ink),
            pair("muted on background", EifiColors.Muted, EifiColors.Background),
            pair("muted on surface", EifiColors.Muted, EifiColors.Surface),
            pair("muted on divider (disabled button)", EifiColors.Muted, EifiColors.Divider),
            pair("on-primary on primary", EifiColors.OnPrimary, EifiColors.Primary),
            pair("on-primary on background (secondary button)", EifiColors.OnPrimary, EifiColors.Background),
            pair("error on surface", EifiColors.Error, EifiColors.Surface),
            pair("error on background (field inside a card)", EifiColors.Error, EifiColors.Background),
        )

        private fun pair(name: String, foreground: Color, background: Color): Arguments =
            Arguments.of(Named.of(name, foreground.toArgb()), background.toArgb())
    }
}
