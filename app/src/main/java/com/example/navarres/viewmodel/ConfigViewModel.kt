package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. Creamos los 3 estados posibles
enum class AppThemeMode {
    SYSTEM, // Sigue al móvil
    LIGHT,  // Siempre claro
    DARK    // Siempre oscuro
}

class ConfigViewModel : ViewModel() {

    // Ahora guardamos el MODO, no si es oscuro o no
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode = _themeMode.asStateFlow()

    // ... (Tus variables de idioma y fuente se quedan igual) ...
    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale = _fontScale.asStateFlow()

    private val _currentLanguage = MutableStateFlow("es")
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _uiStrings = MutableStateFlow<Map<String, String>>(emptyMap())
    val uiStrings = _uiStrings.asStateFlow()

    private val translations = mapOf(
        "es" to mapOf(
            // ... tus traducciones ...
            "nav_rest" to "Restaurantes",
            "dark_mode" to "Modo Oscuro",
            "theme_system" to "Sistema",
            "theme_light" to "Claro",
            "theme_dark" to "Oscuro"
        )
        // ... añade las claves "theme_..." a los otros idiomas también
    )

    init {
        updateLanguage("es")
    }

    // Función para cambiar entre los 3 modos
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    // ... (Resto de funciones de idioma y fuente igual) ...
    fun updateLanguage(lang: String) {
        _currentLanguage.value = lang
        _uiStrings.value = translations[lang] ?: translations["es"]!!
    }

    fun updateFontScale(scale: Float) {
        _fontScale.value = scale
    }
}