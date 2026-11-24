package com.chronodisrup.chronoplay.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ChronoOrange = Color(0xFFFF5A36)
private val ChronoBlack = Color(0xFF090909)
private val ChronoSurface = Color(0xFF141414)
private val ChronoText = Color(0xFFF5F5F5)
private val ChronoMuted = Color(0xFF929292)

private val Colors = darkColorScheme(
    primary = ChronoOrange,
    onPrimary = Color.Black,
    background = ChronoBlack,
    onBackground = ChronoText,
    surface = ChronoSurface,
    onSurface = ChronoText,
    secondary = ChronoMuted
)

@Composable
fun ChronoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
