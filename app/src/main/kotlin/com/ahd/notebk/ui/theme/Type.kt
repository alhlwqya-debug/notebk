package com.ahd.notebk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Readable Arabic-first typography for the whole app.
 * Uses Android's bundled sans-serif font so no external font download is required.
 */
private val AppFont = FontFamily.SansSerif

val NotebkTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppFont, fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = AppFont, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontFamily = AppFont, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),

    headlineLarge = TextStyle(fontFamily = AppFont, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = AppFont, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontFamily = AppFont, fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),

    titleLarge = TextStyle(fontFamily = AppFont, fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = AppFont, fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = AppFont, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),

    bodyLarge = TextStyle(fontFamily = AppFont, fontSize = 16.sp, lineHeight = 25.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = AppFont, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = AppFont, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),

    labelLarge = TextStyle(fontFamily = AppFont, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = AppFont, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = AppFont, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
)
