package com.eifi.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * TODO(EIFI-011) : embarquer Plus Jakarta Sans (OFL) dans res/font et remplacer FontFamily.Default.
 */
internal val EifiFontFamily: FontFamily = FontFamily.Default

/** Échelle : montant 68/800 → étiquettes 13/600, corps 15/500. */
internal val EifiTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = EifiFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 68.sp,
        letterSpacing = (-0.04).em,
    ),
    headlineMedium = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    labelMedium = TextStyle(fontFamily = EifiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
)
