package com.example.navarres.viewmodel

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.LocationRepository
import com.example.navarres.model.repository.RestaurantRepository
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RestaurantesViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository()
    private val restaurantRepo = RestaurantRepository()
    private val locationRepo = LocationRepository(application)
    private val auth = FirebaseAuth.getInstance()

    // --- DATOS ---
    private val _listaMemoria = mutableListOf<Restaurant>()
    val listaVisualizable = mutableStateListOf<Restaurant>()
    val favoritosState = mutableStateMapOf<String, Boolean>()

    // --- ESTADOS DE FILTROS ---
    var searchText by mutableStateOf("")
        private set
    var filterRating by mutableStateOf(0)
        private set
    var filterPrice by mutableStateOf(0)
        private set

    // --- UBICACIÓN ---
    var userLocation by mutableStateOf<Location?>(null)
        private set

    private val defaultLat = 42.8137
    private val defaultLon = -1.6406

    var isLoading by mutableStateOf(false)
        private set
    private var searchJob: Job? = null

    init {
        cargarDatosIniciales()
        loadUserFavorites()
    }

    // 1. OBTENER UBICACIÓN
    fun refreshLocationAndSort() {
        viewModelScope.launch {
            val loc = locationRepo.getUserLocation()
            if (loc != null) {
                userLocation = loc
                aplicarFiltrosYOrden()
            }
        }
    }

    // --- NUEVO: Función para DESACTIVAR el filtro de ubicación ---
    fun clearLocationFilter() {
        userLocation = null
        aplicarFiltrosYOrden() // Reordena usando defaultLat/Lon
    }

    // 2. CARGA DE DATOS
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            isLoading = true
            try {
                val todos = restaurantRepo.obtenerRestaurantesGenericos(50)
                _listaMemoria.clear()
                _listaMemoria.addAll(todos)

                // Intentamos location al inicio, pero si falla no pasa nada
                refreshLocationAndSort()
                aplicarFiltrosYOrden()
            } catch (e: Exception) {
                Log.e("VM", "Error carga: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // 3. BUSCADOR
    fun onSearchTextChange(text: String) {
        searchText = text
        searchJob?.cancel()

        if (text.isEmpty()) {
            cargarDatosIniciales()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            isLoading = true
            try {
                val resultados = restaurantRepo.nombreEmpiezaPor(text.replaceFirstChar { it.uppercase() })
                _listaMemoria.clear()
                _listaMemoria.addAll(resultados)
                aplicarFiltrosYOrden()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 4. FILTROS
    fun updateFilters(rating: Int, price: Int) {
        filterRating = rating
        filterPrice = price
        aplicarFiltrosYOrden()
    }

    // 5. LÓGICA CENTRAL
    private fun aplicarFiltrosYOrden() {
        // A) FILTRADO
        val listaFiltrada = _listaMemoria.filter { res ->
            val cumpleRating = if (filterRating == 0) true else res.valoracion >= filterRating.toDouble()
            val nivelPrecio = calcularNivelPrecio(res.precio)
            val cumplePrecio = if (filterPrice == 0) true else nivelPrecio <= filterPrice
            cumpleRating && cumplePrecio
        }

        // B) ORDENAMIENTO
        // Si userLocation es null, usa defaultLat/Lon (Pamplona)
        val latRef = userLocation?.latitude ?: defaultLat
        val lonRef = userLocation?.longitude ?: defaultLon

        val listaOrdenada = listaFiltrada.sortedBy { res ->
            val results = FloatArray(1)
            Location.distanceBetween(latRef, lonRef, res.latitud, res.longitud, results)
            results[0]
        }

        listaVisualizable.clear()
        listaVisualizable.addAll(listaOrdenada)
    }

    // --- UTILIDADES ---
    fun calculateDistanceStr(latDest: Double, lonDest: Double): String {
        val latOrigen = userLocation?.latitude ?: defaultLat
        val lonOrigen = userLocation?.longitude ?: defaultLon

        val results = FloatArray(1)
        Location.distanceBetween(latOrigen, lonOrigen, latDest, lonDest, results)
        return "${String.format("%.1f", results[0] / 1000)} km"
    }

    private fun calcularNivelPrecio(precioStr: String): Int {
        if (precioStr.isBlank()) return 2
        return try {
            val limpio = precioStr.replace("€", "").trim()
            if (limpio.contains("-")) {
                val partes = limpio.split("-")
                val min = partes[0].trim().toDoubleOrNull() ?: 0.0
                val max = partes[1].trim().toDoubleOrNull() ?: 0.0
                precioPorMedia((min + max) / 2)
            } else {
                precioPorMedia(limpio.toDoubleOrNull() ?: 25.0)
            }
        } catch (e: Exception) { 2 }
    }

    private fun precioPorMedia(media: Double): Int {
        return when {
            media < 20 -> 1
            media < 45 -> 2
            else -> 3
        }
    }

    // --- FAVORITOS ---
    private fun loadUserFavorites() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try { userRepo.getUserFavorites(uid).forEach { favoritosState[it] = true } } catch (e: Exception) {}
        }
    }

    fun toggleFavorite(res: Restaurant) {
        val uid = auth.currentUser?.uid ?: return
        val id = res.nombre
        val newStatus = !(favoritosState[id] ?: false)
        favoritosState[id] = newStatus
        viewModelScope.launch { try { userRepo.toggleFavorite(uid, id, newStatus) } catch (e: Exception) {} }
    }

    fun getRestaurantById(nombre: String): Restaurant? = listaVisualizable.find { it.nombre == nombre }
}