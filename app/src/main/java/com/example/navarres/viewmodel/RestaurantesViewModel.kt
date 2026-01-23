package com.example.navarres.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant // Tu data class
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RestaurantesViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    // Usamos TU clase Restaurant. He creado datos ficticios para que compile.
    val localesDePrueba = mutableStateListOf(
        Restaurant(
            identificador = "rest_redin_01",
            nombre = "El Redín",
            categoria = "Bar",
            municipio = "Pamplona",
            direccion = "Calle del Mercado"
        ),
        Restaurant(
            identificador = "rest_navarres_02",
            nombre = "Taberna NavarRes",
            categoria = "Restaurante",
            municipio = "Pamplona"
        ),
        Restaurant(
            identificador = "rest_vina_03",
            nombre = "La Viña",
            categoria = "Asador",
            municipio = "Pamplona"
        )
    )

    // Mapa: Identificador (String) -> ¿Es favorito? (Boolean)
    val favoritosState = mutableStateMapOf<String, Boolean>()

    // INIT: Se ejecuta nada más crear el ViewModel
    init {
        loadUserFavorites()
    }

    // Descarga la lista de favoritos de Firebase al iniciar
    private fun loadUserFavorites() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val favList = repository.getUserFavorites(uid)
            // Actualizamos el mapa local: Ponemos 'true' a los IDs que vienen de Firebase
            favList.forEach { idRestaurante ->
                favoritosState[idRestaurante] = true
            }
        }
    }

    // Acción del usuario
    fun toggleFavorite(restaurant: Restaurant) {
        val uid = auth.currentUser?.uid ?: return
        val id = restaurant.identificador // Usamos tu campo identificador

        // 1. Lógica Optimista (Actualizar UI ya)
        val isCurrentlyFav = favoritosState[id] ?: false
        val newStatus = !isCurrentlyFav
        favoritosState[id] = newStatus

        // 2. Persistir en Firebase
        viewModelScope.launch {
            try {
                repository.toggleFavorite(uid, id, newStatus)
            } catch (e: Exception) {
                // Si falla, revertimos el cambio visual
                favoritosState[id] = isCurrentlyFav
            }
        }
    }
}