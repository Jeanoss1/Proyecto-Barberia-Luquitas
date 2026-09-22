package com.example.barberialuquitas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BarberiaColorScheme = darkColorScheme(
    primary = GoldAccent,
    secondary = TextGray,
    tertiary = InputBackground,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = DarkBackground,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun BarberiaLuquitasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BarberiaColorScheme,
        typography = Typography,
        content = content
    )
}
