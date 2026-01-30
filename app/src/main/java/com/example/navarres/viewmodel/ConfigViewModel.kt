package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfigViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale = _fontScale.asStateFlow()

    private val _currentLanguage = MutableStateFlow("es")
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _uiStrings = MutableStateFlow<Map<String, String>>(emptyMap())
    val uiStrings = _uiStrings.asStateFlow()

    private val translations = mapOf(
        "es" to mapOf(
            "nav_rest" to "Restaurantes",
            "nav_fav" to "Favoritos",
            "nav_perfil" to "Perfil",
            "nav_config" to "Configuración",
            "nav_lang" to "Idioma",
            "dark_mode" to "Modo Oscuro",
            "font_size" to "Tamaño de fuente",
            "size_small" to "Pequeño",
            "size_medium" to "Mediano",
            "size_large" to "Grande"
        ),
        "en" to mapOf(
            "nav_rest" to "Restaurants",
            "nav_fav" to "Favorites",
            "nav_perfil" to "Profile",
            "nav_config" to "Settings",
            "nav_lang" to "Language",
            "dark_mode" to "Dark Mode",
            "font_size" to "Font Size",
            "size_small" to "Small",
            "size_medium" to "Medium",
            "size_large" to "Large"
        ),
        "eu" to mapOf(
            "nav_rest" to "Jatetxeak",
            "nav_fav" to "Gogokoak",
            "nav_perfil" to "Profila",
            "nav_config" to "Ezarpenak",
            "nav_lang" to "Hizkuntza",
            "dark_mode" to "Modu Iluna",
            "font_size" to "Letra-tamaina",
            "size_small" to "Txikia",
            "size_medium" to "Ertaina",
            "size_large" to "Handia"
        )
    )

    init {
        updateLanguage("es")
    }

    fun updateLanguage(lang: String) {
        _currentLanguage.value = lang
        _uiStrings.value = translations[lang] ?: translations["es"]!!
    }

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    fun updateFontScale(scale: Float) {
        _fontScale.value = scale
    }
}