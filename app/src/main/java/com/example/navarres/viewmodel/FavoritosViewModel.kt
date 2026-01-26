package com.example.navarres.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoritosViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Lista de objetos Restaurant reales que vienen de la DB
    val listaFavoritos = mutableStateListOf<Restaurant>()

    init {
        cargarFavoritos()
    }

    fun cargarFavoritos() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // 1. Obtenemos los nombres que el usuario tiene guardados en su lista de favoritos en Firebase
                val nombresFavoritos = repository.getUserFavorites(uid)

                if (nombresFavoritos.isEmpty()) {
                    listaFavoritos.clear()
                    return@launch
                }

                // 2. Buscamos los datos REALES de esos restaurantes en la colección global
                // Filtramos la colección 'restaurantes' donde el campo 'nombre' esté en nuestra lista
                val snapshot = db.collection("restaurantes")
                    .whereIn("nombre", nombresFavoritos)
                    .get()
                    .await()

                val restaurantesReales = snapshot.toObjects(Restaurant::class.java)

                // 3. Actualizamos la lista de la pantalla
                listaFavoritos.clear()
                listaFavoritos.addAll(restaurantesReales)

            } catch (e: Exception) {
                android.util.Log.e("FAVS", "Error al cargar favoritos: ${e.message}")
            }
        }
    }

    fun eliminarFavorito(restaurant: Restaurant) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // 1. Borrar de la lista de favoritos del usuario en Firebase (usando el nombre)
                repository.toggleFavorite(uid, restaurant.nombre, isAdding = false)

                // 2. Quitar de la lista de la pantalla para que el cambio sea instantáneo
                listaFavoritos.remove(restaurant)
            } catch (e: Exception) {
                android.util.Log.e("FAVS", "Error al eliminar favorito: ${e.message}")
            }
        }
    }
}