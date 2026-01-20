// model/network/RestaurantApiService.kt
package com.example.navarres.model.network


import com.example.navarres.model.data.Restaurant
import retrofit2.http.GET

interface RestaurantApiService {



    @GET("restaurantes")
    suspend fun getRestaurants(): List<Restaurant>
}