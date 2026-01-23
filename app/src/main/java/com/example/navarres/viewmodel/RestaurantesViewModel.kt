package com.example.navarres.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.navarres.model.data.Restaurant

class RestaurantesViewModel : ViewModel() {

    // Ahora usamos una lista de objetos reales
    val listaRestaurantes = mutableStateListOf(
        Restaurant(
            id = 1,
            nombre = "El Redín",
            categoria = "Asador Tradicional",
            direccion = "Calle del Mercado, 5",
            localidad = "Pamplona",
            municipio = "Navarra",
            especialidad = listOf("Chuletón", "Pimientos del Piquillo"),
            latitud = 42.8182
        ),
        Restaurant(
            id = 2,
            nombre = "Taberna NavarRes",
            categoria = "Tapas & Pinchos",
            direccion = "Plaza del Castillo, 12",
            localidad = "Pamplona",
            municipio = "Navarra",
            especialidad = listOf("Friticos", "Vino Foral"),
            latitud = 42.8160
        )
    )

    // Mapeo auxiliar solo para la puntuación y favoritos del demo
    val favoritosState = mutableStateMapOf<String, Boolean>()

    fun toggleFavorite(nombre: String) {
        val isFav = favoritosState[nombre] ?: false
        favoritosState[nombre] = !isFav
    }
}