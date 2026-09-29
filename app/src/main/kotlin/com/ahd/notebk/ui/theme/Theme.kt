package com.ahd.notebk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NotebkLightColors = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryGreen,
    error = ErrorRed,
    background = LightBackground,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onError = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun NotebkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NotebkLightColors,
        typography = NotebkTypography,
        content = content
    )
}
