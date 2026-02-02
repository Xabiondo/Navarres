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
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.LocationRepository
import com.example.navarres.model.repository.RestaurantRepository
import com.example.navarres.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestauranteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepo = LocationRepository(application)
    private val restaurantRepo = RestaurantRepository()
    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant = _selectedRestaurant.asStateFlow()

    private val _isOwner = MutableStateFlow(false)
    val isOwner = _isOwner.asStateFlow()

    var userLocation by mutableStateOf<Location?>(null)
        private set

    private val defaultLat = 42.8137
    private val defaultLon = -1.6406

    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
        checkOwnership(restaurant)
        refreshLocation()
    }

    private fun checkOwnership(restaurant: Restaurant) {
        val fbUser = authRepo.getCurrentUser()
        if (fbUser != null) {
            viewModelScope.launch {
                val userData = userRepo.getUser(fbUser.uid)

                // LOGS DE PRUEBA: Mira esto en el Logcat de Android Studio
                Log.d("NAV_DEBUG", "--------------------------------------")
                Log.d("NAV_DEBUG", "UID Usuario: ${fbUser.uid}")
                Log.d("NAV_DEBUG", "Campo 'ownerOf' en Firebase: ${userData?.ownerOf}")
                Log.d("NAV_DEBUG", "ID del Restaurante actual: ${restaurant.id}")

                // Comparación real
                val esDueño = userData?.ownerOf == restaurant.id && restaurant.id.isNotEmpty()
                _isOwner.value = esDueño

                Log.d("NAV_DEBUG", "RESULTADO: ¿Es dueño?: $esDueño")
                Log.d("NAV_DEBUG", "--------------------------------------")
            }
        } else {
            _isOwner.value = false
        }
    }

    fun updateRestaurantData(updatedRestaurant: Restaurant, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = restaurantRepo.actualizarDatosRestaurante(
                restaurantId = updatedRestaurant.id,
                updates = mapOf(
                    "telefono" to updatedRestaurant.telefono,
                    "precio" to updatedRestaurant.precio,
                    "horarios" to updatedRestaurant.horarios
                )
            )
            if (success) _selectedRestaurant.value = updatedRestaurant
            onResult(success)
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