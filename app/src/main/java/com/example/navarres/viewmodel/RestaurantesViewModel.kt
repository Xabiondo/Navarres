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

    // Lista unificada: Usamos 'identificador' (de main) y los datos detallados (de ivan)
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
    // Usamos el estado reactivo de Compose para que la UI se actualice sola
    val favoritosState = mutableStateMapOf<String, Boolean>()

    init {
        loadUserFavorites()
    }

    // Descarga la lista de favoritos de Firebase al iniciar
    private fun loadUserFavorites() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val favList = repository.getUserFavorites(uid)
                // Actualizamos el mapa local con los IDs que vienen de la nube
                favList.forEach { idRestaurante ->
                    favoritosState[idRestaurante] = true
                }
            } catch (e: Exception) {
                // Manejar error de red si es necesario
            }
        }
    }

    // Acción del usuario: Toggle con lógica optimista
    fun toggleFavorite(restaurant: Restaurant) {
        val uid = auth.currentUser?.uid ?: return
        val id = restaurant.identificador

        // 1. Actualización rápida en la UI (Lógica optimista)
        val isCurrentlyFav = favoritosState[id] ?: false
        val newStatus = !isCurrentlyFav
        favoritosState[id] = newStatus

        // 2. Persistir el cambio en Firebase mediante el repositorio
        viewModelScope.launch {
            try {
                repository.toggleFavorite(uid, id, newStatus)
            } catch (e: Exception) {
                // Si la red falla, revertimos el cambio en la pantalla para no engañar al usuario
                favoritosState[id] = isCurrentlyFav
            }
        }
    }
}