package com.example.navarres.model.repository

import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.network.RestaurantApiService

class RestaurantRepository(private val api: RestaurantApiService) {

    // Esta función es la que llamará el ViewModel
    suspend fun getAllRestaurants(): Result<List<Restaurant>> {
        return try {
            // 1. Llamada a la API (se suspende aquí hasta recibir datos)
            val response = api.getRestaurants()

            // 2. Si todo va bien, devolvemos éxito
            Result.success(response)

        } catch (e: Exception) {
            // 3. Si falla (sin internet, servidor caído), devolvemos fallo
            Result.failure(e)
        }
    }
}