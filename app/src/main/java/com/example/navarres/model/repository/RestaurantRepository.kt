package com.example.navarres.model.repository

import android.util.Log
import com.example.navarres.model.data.Restaurant
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class RestaurantRepository {

    private val db = Firebase.firestore
    private val collectionRef = db.collection("restaurantes")

    suspend fun busquedaPersonalizada(
        especialidad: String?,
        localidad: String?,
        modalidad: String?,
        limite: Int?
    ): List<Restaurant> {
        var query: Query = collectionRef

        if (!especialidad.isNullOrEmpty()) {
            query = query.whereArrayContains("especialidad", especialidad)
        }
        if (!localidad.isNullOrEmpty()) {
            query = query.whereEqualTo("localidad", localidad)
        }
        if (!modalidad.isNullOrEmpty()) {
            query = query.whereEqualTo("modalidad", modalidad)
        }
        if (limite != null && limite > 0) {
            query = query.limit(limite.toLong())
        }

        return ejecutarConsulta(query)
    }

    suspend fun obtenerUltimosInscritos(limit: Int): List<Restaurant> {
        val query = collectionRef
            .orderBy("fechaInscripcion", Query.Direction.DESCENDING)
            .limit(limit.toLong())
        return ejecutarConsulta(query)
    }

    suspend fun obtenerRestaurantesGenericos(limit: Int): List<Restaurant> {
        val query = collectionRef.limit(limit.toLong())
        return ejecutarConsulta(query)
    }

// ... (tus imports actuales)

    suspend fun nombreEmpiezaPor(query: String): List<Restaurant> {
        return try {
            // Normalizamos la búsqueda: Primera en mayúscula, resto minúscula (Ej: "Alhambra")
            val formattedQuery = query.lowercase().replaceFirstChar { it.uppercase() }

            val snapshot = db.collection("restaurantes")
                .orderBy("nombre")
                .startAt(formattedQuery)
                .endAt(formattedQuery + "\uf8ff")
                .get()
                .await()

            snapshot.toObjects(Restaurant::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // HELPER: Mapea el Document ID (ej: UR000005) al campo 'id' del objeto
    private suspend fun ejecutarConsulta(query: Query): List<Restaurant> {
        return try {
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                val restaurant = doc.toObject(Restaurant::class.java)
                // Esto copia el nombre del documento de Firebase al campo ID de la clase
                restaurant?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("RestaurantRepo", "Error en consulta: ${e.message}")
            emptyList()
        }
    }

    suspend fun actualizarDatosRestaurante(restaurantId: String, updates: Map<String, Any>): Boolean {
        if (restaurantId.isEmpty()) return false
        return try {
            db.collection("restaurantes").document(restaurantId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("RestaurantRepo", "Error actualizando doc $restaurantId: ${e.message}")
            false
        }
    }
}