package com.minimax.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB69CFF),
    onPrimary = Color(0xFF24104C),
    primaryContainer = Color(0xFF3C246B),
    onPrimaryContainer = Color(0xFFEBDDFF),
    secondary = Color(0xFF9DD7FF),
    background = Color(0xFF0D0F17),
    onBackground = Color(0xFFE8E6F0),
    surface = Color(0xFF11131C),
    onSurface = Color(0xFFE8E6F0),
    surfaceVariant = Color(0xFF242633),
    onSurfaceVariant = Color(0xFFC5C1D0),
    outline = Color(0xFF8D899A),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6F45C1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF),
    onPrimaryContainer = Color(0xFF24104C),
    secondary = Color(0xFF006A92),
    background = Color(0xFFF9F8FF),
    surface = Color(0xFFF9F8FF),
)

@Composable
fun MiniMaxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
