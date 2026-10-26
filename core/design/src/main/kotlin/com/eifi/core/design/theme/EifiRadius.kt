package com.eifi.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Radii: buttons 18, cards 22 to 28. */
object EifiRadius {
    val Button = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(22.dp)
    val CardLarge = RoundedCornerShape(28.dp)
}

internal val EifiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = EifiRadius.Button,
    large = EifiRadius.Card,
    extraLarge = EifiRadius.CardLarge,
)
