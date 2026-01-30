package com.example.navarres.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.navarres.model.data.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RestauranteDetailViewModel : ViewModel() {
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant = _selectedRestaurant.asStateFlow()

    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
    }

    fun getDistanceToUser(resLat: Double, resLon: Double): String {
        val merindadesLat = 42.8137
        val merindadesLon = -1.6406
        val results = FloatArray(1)
        Location.distanceBetween(merindadesLat, merindadesLon, resLat, resLon, results)
        return "${String.format("%.1f", results[0] / 1000)} km"
    }
}