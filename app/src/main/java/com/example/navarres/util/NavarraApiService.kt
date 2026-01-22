import retrofit2.http.GET

interface NavarraApiService {
    // URL del recurso JSON que subiste
    @GET("api/3/action/datastore_search?resource_id=0a197290-2e3b-4b0c-b383-4f1ec46d9468")
    suspend fun obtenerRestaurantes(): ApiResponse
}


data class ApiResponse(
    val success: Boolean,
    val result: ApiResult
)

data class ApiResult(
    val records: List<List<Any>>
)