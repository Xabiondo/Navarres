package com.example.navarres.viewmodel

import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Cambiamos a AndroidViewModel para poder usar 'application'
class RestauranteDetailViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Copiamos la lógica del Repositorio de tu otro ViewModel
    private val locationRepo = LocationRepository(application)

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant = _selectedRestaurant.asStateFlow()

    // 3. Variable de ubicación propia (Igual que en RestaurantesViewModel)
    var userLocation by mutableStateOf<Location?>(null)
        private set

    private val defaultLat = 42.8137
    private val defaultLon = -1.6406

    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
        // Al entrar, pedimos ubicación fresca
        refreshLocation()
    }

    // 4. Función para pedir el GPS (Copiada de tu lógica)
    private fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationRepo.getUserLocation()
            if (loc != null) {
                userLocation = loc
            }
        }
    }

    // 5. Cálculo usando la ubicación interna
    fun calculateDistanceStr(latDest: Double, lonDest: Double): String {
        val latOrigen = userLocation?.latitude ?: defaultLat
        val lonOrigen = userLocation?.longitude ?: defaultLon

        val results = FloatArray(1)
        Location.distanceBetween(latOrigen, lonOrigen, latDest, lonDest, results)

        val metros = results[0]
        val km = metros / 1000

        return if (km < 1) {
            "${metros.toInt()} m"
        } else {
            "${String.format("%.1f", km)} km"
        }
    }
}