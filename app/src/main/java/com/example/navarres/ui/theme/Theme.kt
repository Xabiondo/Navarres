package com.example.navarres.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ... (Tus definiciones de colores Light/Dark van aquí arriba igual que antes) ...
// ... Copia los colores que te pasé en la respuesta anterior ...

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

val DefaultTypography = Typography()

@Composable
fun NavarresTheme(
    darkTheme: Boolean, // AQUI NO ponemos valor por defecto, se lo pasaremos desde el Main
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val scaledTypography = Typography(
        headlineMedium = DefaultTypography.headlineMedium.copy(fontSize = (DefaultTypography.headlineMedium.fontSize.value * fontScale).sp),
        headlineSmall = DefaultTypography.headlineSmall.copy(fontSize = (DefaultTypography.headlineSmall.fontSize.value * fontScale).sp),
        titleLarge = DefaultTypography.titleLarge.copy(fontSize = (DefaultTypography.titleLarge.fontSize.value * fontScale).sp),
        titleMedium = DefaultTypography.titleMedium.copy(fontSize = (DefaultTypography.titleMedium.fontSize.value * fontScale).sp),
        bodyLarge = DefaultTypography.bodyLarge.copy(fontSize = (DefaultTypography.bodyLarge.fontSize.value * fontScale).sp),
        bodyMedium = DefaultTypography.bodyMedium.copy(fontSize = (DefaultTypography.bodyMedium.fontSize.value * fontScale).sp),
        labelLarge = DefaultTypography.labelLarge.copy(fontSize = (DefaultTypography.labelLarge.fontSize.value * fontScale).sp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}