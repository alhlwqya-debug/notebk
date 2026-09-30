package com.ahd.notebk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NotebkLightColors = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryGreen,
    tertiary = AccentBlue,
    error = ErrorRed,
    background = LightBackground,
    surface = SurfaceWhite,
    surfaceVariant = Color(0xFFEDECF4),
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onTertiary = SurfaceWhite,
    onError = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val NotebkDarkColors = darkColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryGreen,
    tertiary = AccentCyan,
    error = ErrorRed,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF001A20),
    onError = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary
)

@Composable
fun NotebkTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) NotebkDarkColors else NotebkLightColors,
        typography = NotebkTypography,
        content = content
    )
}
