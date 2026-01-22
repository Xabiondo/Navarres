package com.example.navarres.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NavarraRed,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    secondary = PurpleGrey80
)

private val LightColorScheme = lightColorScheme(
    primary = NavarraRed,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    secondary = PurpleGrey40
)

@Composable
fun NavarresTheme(
    darkTheme: Boolean, // Ahora lo controlamos desde el ViewModel
    content: @Composable () -> Unit
) {
    // Forzamos el uso de nuestras paletas personalizadas
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}