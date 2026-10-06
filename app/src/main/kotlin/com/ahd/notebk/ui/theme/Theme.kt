package com.ahd.notebk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val NotebkLightColors = lightColorScheme(
    primary = Color(0xFF9C4DFF),
    secondary = Color(0xFF39D98A),
    tertiary = Color(0xFF58A6FF),
    error = ErrorRed,
    background = Color(0xFF070019),
    surface = Color(0xFF12072E),
    surfaceVariant = Color(0xFF190B3E),
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onTertiary = SurfaceWhite,
    onError = SurfaceWhite,
    onBackground = Color(0xFFF9F5FF),
    onSurface = Color(0xFFF9F5FF)
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
fun NotebkTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) NotebkDarkColors else NotebkLightColors,
        typography = NotebkTypography,
        shapes = androidx.compose.material3.Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
