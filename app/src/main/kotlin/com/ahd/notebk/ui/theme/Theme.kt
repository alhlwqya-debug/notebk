package com.ahd.notebk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.ahd.notebk.ui.components.setNotebkPalette
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val NotebkLightColors = lightColorScheme(
    primary = Color(0xFF7C3AED),
    primaryContainer = Color(0xFFF0E7FF),
    onPrimaryContainer = Color(0xFF32105F),
    secondary = Color(0xFF22B573),
    tertiary = Color(0xFF3B82D0),
    error = ErrorRed,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF8F6FC),
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onTertiary = SurfaceWhite,
    onError = SurfaceWhite,
    onBackground = Color(0xFF211B2B),
    onSurface = Color(0xFF211B2B),
    // Darker than the previous muted lavender for clearer small text.
    onSurfaceVariant = Color(0xFF51465F),
    outline = Color(0xFF8E7DA2),
    outlineVariant = Color(0xFFC8B9D9)
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
    onSurfaceVariant = Color(0xFFD5D3E0),
    outline = Color(0xFF8E8A9D),
    outlineVariant = Color(0xFF514D60)
)

@Composable
fun NotebkTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    SideEffect { setNotebkPalette(darkTheme) }
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
