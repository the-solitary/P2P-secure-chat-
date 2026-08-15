package com.jorge.p2pchat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D5B),
    secondary = Color(0xFF4A6C6F),
    background = Color(0xFFFAFAF7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD1AA),
    secondary = Color(0xFF9BC2C4),
    background = Color(0xFF121412)
)

@Composable
fun P2PChatTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
