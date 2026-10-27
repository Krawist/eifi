package com.eifi.core.design.theme

import androidx.compose.ui.graphics.Color

/** Colour tokens of the design guidelines (plain version). See docs/dossier-technique.md, "Design guidelines". */
object EifiColors {
    val Primary = Color(0xFF00DBAD)
    val OnPrimary = Color(0xFF04362B)
    val Accent = Color(0xFFFEC447)
    val OnAccent = Color(0xFF3A2A00)
    val Soft = Color(0xFFDDFDBC)
    val OnSoft = Color(0xFF1F3A12)
    val Background = Color(0xFFF6F8F7)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF0E1A17)
    val Muted = Color(0xFF56665F)
    val Divider = Color(0xFFEEF1F0)

    /** Light tints of [Primary] and [Accent], behind icons only (see the onboarding mockups). */
    val PrimarySoft = Color(0xFFE0F9F2)
    val AccentSoft = Color(0xFFFFF0CC)

    /** Light [Primary], for elements laid on a [Primary] card. */
    val PrimaryLight = Color(0xFF7AEBD3)
}
