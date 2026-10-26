package com.eifi.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Rayons : boutons 18, cartes 22 à 28. */
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
