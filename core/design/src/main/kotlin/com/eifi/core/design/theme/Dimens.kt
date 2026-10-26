package com.eifi.core.design.theme

import androidx.compose.ui.unit.dp

/** Spacing scale, on a 4 dp grid. Screens use [Gutter] as horizontal margin. */
object EifiSpacing {
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val Gutter = 20.dp
    val XLarge = 24.dp
    val XXLarge = 32.dp
}

/** Component sizes taken from the mockups. */
object EifiDimens {
    /** Minimum touch target of any interactive element. */
    val MinTouchTarget = 44.dp
    val ButtonHeight = 58.dp
    val KeypadKeyHeight = 62.dp
    val KeypadGap = 6.dp
    val PillHeight = 28.dp
    val PillIcon = 14.dp
}
