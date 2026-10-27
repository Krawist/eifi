package com.eifi.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Radii: buttons 18, cards 22 to 28. */
object EifiRadius {
    /** Corner of [CardLarge], for outlines drawn by hand. */
    internal val CardLargeCorner = 28.dp

    val Button = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(22.dp)
    val CardLarge = RoundedCornerShape(CardLargeCorner)
}

internal val EifiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = EifiRadius.Button,
    large = EifiRadius.Card,
    extraLarge = EifiRadius.CardLarge,
)
