package com.example.navarres.viewmodel

import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.data.RestaurantStats
import com.example.navarres.model.repository.CommentRepository
import com.example.navarres.model.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestauranteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepo = LocationRepository(application)
    private val commentRepo = CommentRepository()

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant = _selectedRestaurant.asStateFlow()

    private val _stats = MutableStateFlow(RestaurantStats())
    val stats = _stats.asStateFlow()

    var userLocation by mutableStateOf<Location?>(null)
        private set

    private val defaultLat = 42.8137
    private val defaultLon = -1.6406

    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
        refreshLocation()
        calculateStats(restaurant.id)
    }

    // --- NUEVO: Función pública para forzar la actualización ---
    fun refreshStats() {
        val currentId = _selectedRestaurant.value?.id
        if (currentId != null) {
            calculateStats(currentId)
        }
    }

    private fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationRepo.getUserLocation()
            if (loc != null) {
                userLocation = loc
            }
        }
    }

    private fun calculateStats(restaurantId: String) {
        viewModelScope.launch {
            val comments = commentRepo.obtenerComentariosPorRestaurante(restaurantId)

            if (comments.isEmpty()) {
                _stats.value = RestaurantStats()
                return@launch
            }

            // --- CORRECCIÓN CLAVE: FILTRAR SOLO COMENTARIOS PADRE ---
            // Las respuestas (parentId != null) NO deben contar para la media
            val reviewsReales = comments.filter { it.parentId == null }

            if (reviewsReales.isEmpty()) {
                // Si solo hay respuestas pero no reseñas principales (raro, pero posible)
                _stats.value = RestaurantStats()
                return@launch
            }

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