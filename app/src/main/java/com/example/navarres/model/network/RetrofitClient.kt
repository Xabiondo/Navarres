// model/network/RetrofitClient.kt
package com.example.navarres.model.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.tudominio.com/" // Tu URL base

    val apiService: RestaurantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Para convertir JSON a Objetos
            .build()
            .create(RestaurantApiService::class.java)
        
    }
}