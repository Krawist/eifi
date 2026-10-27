package com.eifi.core.design.component

import androidx.compose.ui.graphics.Color
import com.eifi.core.design.theme.EifiColors

/** Colour pairs of [EifiPill], all above 4.5:1. */
enum class EifiPillTone(val container: Color, val content: Color) {
    /** Accepted operators, NFC banner. */
    Soft(container = EifiColors.Soft, content = EifiColors.OnSoft),

    /** Waiting, fees, manual confirmation. */
    Accent(container = EifiColors.Accent, content = EifiColors.OnAccent),

    /** On a [EifiColors.Primary] card, e.g. the NFC status of the home screen. */
    OnPrimary(container = EifiColors.PrimaryLight, content = EifiColors.OnPrimary),

    /** Neutral information. */
    Neutral(container = EifiColors.Divider, content = EifiColors.Ink),
}
