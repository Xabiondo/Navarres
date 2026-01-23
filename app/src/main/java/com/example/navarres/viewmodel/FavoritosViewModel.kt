package com.example.navarres.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritosViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    // Lista de restaurantes FAVORITOS que mostraremos en la pantalla
    val listaFavoritos = mutableStateListOf<Restaurant>()

    // IMPORTANTE: Como aún no tenemos una base de datos de Restaurantes en Firebase,
    // usamos la misma lista estática que tienes en el otro ViewModel como "Fuente de Verdad".
    // En el futuro, esto debería venir de un 'RestaurantRepository'.
    private val todosLosRestaurantes = listOf(
        Restaurant(identificador = "rest_redin_01", nombre = "El Redín", categoria = "Bar", municipio = "Pamplona"),
        Restaurant(identificador = "rest_navarres_02", nombre = "Taberna NavarRes", categoria = "Restaurante", municipio = "Pamplona"),
        Restaurant(identificador = "rest_vina_03", nombre = "La Viña", categoria = "Asador", municipio = "Pamplona")
    )

    init {
        cargarFavoritos()
    }

    fun cargarFavoritos() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // 1. Obtenemos la lista de IDs de Firebase (ej: ["rest_redin_01"])
            val idsFavoritos = repository.getUserFavorites(uid)

            // 2. Filtramos la lista maestra para quedarnos solo con los objetos completos que coinciden
            val restaurantesFiltrados = todosLosRestaurantes.filter { restaurante ->
                idsFavoritos.contains(restaurante.identificador)
            }

            // 3. Actualizamos la lista visible
            listaFavoritos.clear()
            listaFavoritos.addAll(restaurantesFiltrados)
        }
    }

    // Función para quitar de favoritos desde esta misma pantalla
    fun eliminarFavorito(restaurant: Restaurant) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // 1. Borrar de Firebase
            repository.toggleFavorite(uid, restaurant.identificador, isAdding = false)

            // 2. Borrar de la lista local inmediatamente (para que desaparezca de la pantalla)
            listaFavoritos.remove(restaurant)
        }
    }
}