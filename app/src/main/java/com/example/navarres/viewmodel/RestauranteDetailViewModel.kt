package com.example.navarres.viewmodel

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

    // Simulación de cálculo de distancia (en una fase real usaría LocationServices)
    fun getDistanceToUser(resLat: Double, resLon: Double): String {
        return "1.2 km" // Dato ficticio por ahora
    }
}