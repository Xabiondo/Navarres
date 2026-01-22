package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale = _fontScale.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Español")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    // Aquí definimos uiStrings que es lo que la UI observará
    private val _uiStrings = MutableStateFlow(getTranslations("Español"))
    val uiStrings = _uiStrings.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun updateFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
        _uiStrings.value = getTranslations(language)
    }

    private fun getTranslations(lang: String): Map<String, String> {
        return when (lang) {
            "English" -> mapOf(
                "title" to "Settings",
                "appearance" to "Appearance",
                "dark_mode" to "Dark Mode",
                "font_size" to "Font Size",
                "language" to "Language",
                "nav_rest" to "Places",
                "nav_fav" to "Favorites",
                "nav_perfil" to "Profile",
                "nav_config" to "Settings"
            )
            "Euskara" -> mapOf(
                "title" to "Ezarpenak",
                "appearance" to "Itxura",
                "dark_mode" to "Modu iluna",
                "font_size" to "Letra-tamaina",
                "language" to "Hizkuntza",
                "nav_rest" to "Lekuak",
                "nav_fav" to "Gogokoak",
                "nav_perfil" to "Profila",
                "nav_config" to "Ezarpenak"
            )
            else -> mapOf(
                "title" to "Configuración",
                "appearance" to "Apariencia",
                "dark_mode" to "Modo Oscuro",
                "font_size" to "Tamaño de fuente",
                "language" to "Idioma",
                "nav_rest" to "Restaurantes",
                "nav_fav" to "Favoritos",
                "nav_perfil" to "Perfil",
                "nav_config" to "Ajustes"
            )
        }
    }
}