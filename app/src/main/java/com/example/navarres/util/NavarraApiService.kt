package com.example.navarres.model

import retrofit2.http.GET

// 1. La Interfaz con la URL completa
interface NavarraApiService {
    @GET("api/3/action/datastore_search?resource_id=0a197290-2e3b-4b0c-b383-4f1ec46d9468&limit=600")
    suspend fun obtenerRestaurantes(): ApiResponse
}

// 2. El modelo de respuesta principal
data class ApiResponse(
    val success: Boolean,
    val result: ApiResult
)

// 3. El resultado que contiene la lista de restaurantes (CORREGIDO)
data class ApiResult(
    val records: List<RestauranteRaw> // Aquí antes tenías List<List<Any>>, esto es mucho mejor
)

// 4. Los datos específicos de cada restaurante
data class RestauranteRaw(
    val COD_INSCRIPCION: String,
    val NOMBRE: String,
    val DIRECCION: String,
    val LOCALIDAD: String,
    val MUNICIPIO: String,
    val Especialidad: String?,
    val CATEGORIA: String
)
