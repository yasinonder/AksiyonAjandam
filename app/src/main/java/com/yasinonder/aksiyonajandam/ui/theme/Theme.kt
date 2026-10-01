package com.yasinonder.aksiyonajandam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E75),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEEF0),
    onPrimaryContainer = Color(0xFF062F33),
    secondary = Color(0xFF315C74),
    onSecondary = Color.White,
    tertiary = Color(0xFFD97932),
    onTertiary = Color.White,
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF172126),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172126),
    surfaceVariant = Color(0xFFE8EEF1),
    onSurfaceVariant = Color(0xFF46545B),
    outline = Color(0xFF76868E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF78D7DE),
    onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF0B535A),
    onPrimaryContainer = Color(0xFFCDEEF0),
    secondary = Color(0xFFA9CCE0),
    onSecondary = Color(0xFF123544),
    tertiary = Color(0xFFFFB77E),
    onTertiary = Color(0xFF542500),
    background = Color(0xFF0F1518),
    onBackground = Color(0xFFE4EAED),
    surface = Color(0xFF151D21),
    onSurface = Color(0xFFE4EAED),
    surfaceVariant = Color(0xFF243137),
    onSurfaceVariant = Color(0xFFC0CCD1),
    outline = Color(0xFF89979E)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun AksiyonAjandamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
