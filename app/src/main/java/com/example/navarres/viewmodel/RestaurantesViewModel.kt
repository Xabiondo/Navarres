package com.example.navarres.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel

class RestaurantesViewModel : ViewModel() {
    // Datos de prueba rescatados de tu HomeScreen original
    val localesDePrueba = mutableStateListOf(
        Triple("El Redín", "0.5 km", 4),
        Triple("La Viña", "1.2 km", 2),
        Triple("Mesón de la Tortilla", "2.1 km", 3),
        Triple("Taberna NavarRes", "0.2 km", 1)
    )
    val favoritosState = mutableStateMapOf<String, Boolean>()

    fun toggleFavorite(nombre: String) {
        val isFav = favoritosState[nombre] ?: false
        favoritosState[nombre] = !isFav
    }
}