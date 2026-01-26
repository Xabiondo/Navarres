package com.example.navarres.viewmodel

import android.location.Location
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantesViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Lista real de restaurantes desde la DB
    val listaRestaurantes = mutableStateListOf<Restaurant>()
    val favoritosState = mutableStateMapOf<String, Boolean>()

    // Plaza de las Merindades, Pamplona
    private val merindadesLat = 42.8137
    private val merindadesLon = -1.6406

    init {
        cargarRestaurantes()
        loadUserFavorites()
    }

    private fun cargarRestaurantes() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("restaurantes").get().await()
                val todos = snapshot.toObjects(Restaurant::class.java)

                // Filtrar a 10km de distancia
                val filtrados = todos.filter { rest ->
                    val resultados = FloatArray(1)
                    Location.distanceBetween(merindadesLat, merindadesLon, rest.latitud, rest.longitud, resultados)
                    (resultados[0] / 1000) <= 10.0
                }

                listaRestaurantes.clear()
                listaRestaurantes.addAll(filtrados)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
        val id = restaurant.nombre // Usamos nombre como ID

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

    fun getRestaurantById(id: String): Restaurant? {
        return listaRestaurantes.find { it.nombre == id }
    }
}