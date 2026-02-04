package com.example.navarres.viewmodel

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.data.RestaurantStats
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.CommentRepository
import com.example.navarres.model.repository.LocationRepository
import com.example.navarres.model.repository.RestaurantRepository
import com.example.navarres.model.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestauranteDetailViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios fusionados
    private val locationRepo = LocationRepository(application)
    private val restaurantRepo = RestaurantRepository() // De IVAN
    private val authRepo = AuthRepository() // De IVAN
    private val userRepo = UserRepository() // De IVAN
    private val commentRepo = CommentRepository() // De MERGE

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant = _selectedRestaurant.asStateFlow()

    // Estado de Dueño (De IVAN)
    private val _isOwner = MutableStateFlow(false)
    val isOwner = _isOwner.asStateFlow()

    // Estado de Estadísticas (De MERGE)
    private val _stats = MutableStateFlow(RestaurantStats())
    val stats = _stats.asStateFlow()

    var userLocation by mutableStateOf<Location?>(null)
        private set

    private val defaultLat = 42.8137
    private val defaultLon = -1.6406

    private var statsJob: Job? = null

    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
        checkOwnership(restaurant) // Lógica de IVAN
        refreshLocation()
        subscribeToStats(restaurant.id) // Lógica de MERGE
    }

    /**
     * Lógica de IVAN: Comprueba si el usuario es dueño
     */
    private fun checkOwnership(restaurant: Restaurant) {
        val fbUser = authRepo.getCurrentUser()
        if (fbUser != null) {
            viewModelScope.launch {
                // Comprobamos si tu UID coincide con el ownerId que pusiste en Firestore
                val esDuenioPorId = restaurant.ownerId == fbUser.uid

                // Mantenemos tu lógica anterior por seguridad
                val userData = userRepo.getUser(fbUser.uid)
                val esDuenioPorPerfil = userData?.ownerOf == restaurant.id

                val resultadoFinal = (esDuenioPorId || esDuenioPorPerfil) && restaurant.id.isNotEmpty()
                _isOwner.value = resultadoFinal

                Log.d("NAV_DEBUG", "UID Usuario: ${fbUser.uid}")
                Log.d("NAV_DEBUG", "OwnerId en Restaurante: ${restaurant.ownerId}")
                Log.d("NAV_DEBUG", "Resultado Final ¿Es dueño?: $resultadoFinal")
            }
        } else {
            _isOwner.value = false
        }
    }

    /**
     * Lógica de IVAN: Actualiza datos del restaurante
     */
    fun updateRestaurantData(updatedRestaurant: Restaurant, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Aquí enviamos todos los campos que el dueño puede editar
            val success = restaurantRepo.actualizarDatosRestaurante(
                restaurantId = updatedRestaurant.id,
                updates = mapOf(
                    "telefono" to updatedRestaurant.telefono,
                    "precio" to updatedRestaurant.precio,
                    "horarios" to updatedRestaurant.horarios,
                    "direccion" to updatedRestaurant.direccion,
                    "categoria" to updatedRestaurant.categoria
                )
            )

            if (success) {
                // Actualizamos el flujo para que la UI se refresque sola
                _selectedRestaurant.value = updatedRestaurant
            }
            onResult(success)
        }
    }

    // Lógica de MERGE: Ya no hace falta llamar a esto manualmente, es automático
    fun refreshStats() { }

    private fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationRepo.getUserLocation()
            if (loc != null) {
                userLocation = loc
            }
        }
    }

    /**
     * Lógica de MERGE: Suscripción en tiempo real a comentarios/stats
     */
    private fun subscribeToStats(restaurantId: String) {
        statsJob?.cancel()

        statsJob = viewModelScope.launch {
            // Escuchamos el flujo continuo de comentarios
            commentRepo.obtenerFlujoComentarios(restaurantId).collect { comments ->

                if (comments.isEmpty()) {
                    _stats.value = RestaurantStats()
                } else {
                    // Filtramos respuestas (parentId == null)
                    val reviewsReales = comments.filter { it.parentId == null }

                    if (reviewsReales.isEmpty()) {
                        _stats.value = RestaurantStats()
                    } else {
                        val total = reviewsReales.size
                        val sum = reviewsReales.sumOf { it.rating }
                        val average = sum.toDouble() / total
                        val distribution = reviewsReales.groupingBy { it.rating }.eachCount()

                        _stats.value = RestaurantStats(
                            averageRating = average,
                            totalReviews = total,
                            countsPerStar = distribution
                        )
                    }
                }
            }
        }
    }

    fun calculateDistanceStr(latDest: Double, lonDest: Double): String {
        val latOrigen = userLocation?.latitude ?: defaultLat
        val lonOrigen = userLocation?.longitude ?: defaultLon
        val results = FloatArray(1)
        Location.distanceBetween(latOrigen, lonOrigen, latDest, lonDest, results)
        val metros = results[0]
        val km = metros / 1000
        return if (km < 1) "${metros.toInt()} m" else "${String.format("%.1f", km)} km"
    }
}