package com.example.navarres.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

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
    darkTheme: Boolean,
    fontScale: Float = 1.0f, // 1. Recibimos el factor de escala
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // 2. Creamos una tipografía dinámica basada en la escala
    val scaledTypography = Typography(
        headlineMedium = Typography.headlineMedium.copy(
            fontSize = (Typography.headlineMedium.fontSize.value * fontScale).sp
        ),
        headlineSmall = Typography.headlineSmall.copy(
            fontSize = (Typography.headlineSmall.fontSize.value * fontScale).sp
        ),
        titleLarge = Typography.titleLarge.copy(
            fontSize = (Typography.titleLarge.fontSize.value * fontScale).sp
        ),
        titleMedium = Typography.titleMedium.copy(
            fontSize = (Typography.titleMedium.fontSize.value * fontScale).sp
        ),
        bodyLarge = Typography.bodyLarge.copy(
            fontSize = (Typography.bodyLarge.fontSize.value * fontScale).sp
        ),
        bodyMedium = Typography.bodyMedium.copy(
            fontSize = (Typography.bodyMedium.fontSize.value * fontScale).sp
        ),
        labelLarge = Typography.labelLarge.copy(
            fontSize = (Typography.labelLarge.fontSize.value * fontScale).sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography, // 3. Aplicamos la tipografía escalada
        content = content
    )
}