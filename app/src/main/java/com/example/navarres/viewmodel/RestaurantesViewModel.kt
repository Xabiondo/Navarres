package com.example.navarres.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RestaurantesViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    // Lista de restaurantes
    val localesDePrueba = mutableStateListOf(
        Restaurant(
            identificador = "rest_redin_01",
            nombre = "El Redín",
            categoria = "Asador Tradicional",
            direccion = "Calle del Mercado, 5",
            localidad = "Pamplona",
            municipio = "Navarra",
            especialidad = listOf("Chuletón", "Pimientos del Piquillo")
        ),
        Restaurant(
            identificador = "rest_navarres_02",
            nombre = "Taberna NavarRes",
            categoria = "Tapas & Pinchos",
            direccion = "Plaza del Castillo, 12",
            localidad = "Pamplona",
            municipio = "Navarra",
            especialidad = listOf("Friticos", "Vino Foral")
        ),
        Restaurant(
            identificador = "rest_vina_03",
            nombre = "La Viña",
            categoria = "Asador",
            direccion = "Calle Jarauta",
            localidad = "Pamplona",
            municipio = "Navarra",
            especialidad = listOf("Ajos", "Cordero")
        )
    )

    // Mapa: Identificador (String) -> ¿Es favorito? (Boolean)
    val favoritosState = mutableStateMapOf<String, Boolean>()

    init {
        loadUserFavorites()
    }

    private fun loadUserFavorites() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val favList = repository.getUserFavorites(uid)
                favList.forEach { idRestaurante ->
                    favoritosState[idRestaurante] = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFavorite(restaurant: Restaurant) {
        val uid = auth.currentUser?.uid ?: return
        val id = restaurant.identificador

        val isCurrentlyFav = favoritosState[id] ?: false
        val newStatus = !isCurrentlyFav
        favoritosState[id] = newStatus

        viewModelScope.launch {
            try {
                repository.toggleFavorite(uid, id, newStatus)
            } catch (e: Exception) {
                favoritosState[id] = isCurrentlyFav
            }
        }
    }

    // --- ESTA ES LA FUNCIÓN QUE TE FALTABA Y DABA ERROR ---
    fun getRestaurantById(id: String): Restaurant? {
        // Busca en la lista 'localesDePrueba' el que tenga ese ID
        return localesDePrueba.find { it.identificador == id }
    }
}