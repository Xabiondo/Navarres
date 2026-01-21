package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigViewModel : ViewModel() {
    // Estado para el modo oscuro
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    // Estado para el tamaño de fuente (multiplicador)
    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale = _fontScale.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun updateFontScale(scale: Float) {
        _fontScale.value = scale
    }
}