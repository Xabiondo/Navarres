package com.example.navarres.model.repository

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

        // Filtro 1: ¿Tiene esa especialidad en su lista?
        if (!especialidad.isNullOrEmpty()) {
            query = query.whereArrayContains("especialidad", especialidad)
        }

        // Filtro 2: ¿Es de esta localidad? (Ej: "Pamplona")
        if (!localidad.isNullOrEmpty()) {
            query = query.whereEqualTo("localidad", localidad)
        }

        // Filtro 3: ¿Qué modalidad es? (Ej: "Restaurante", "Bar", etc.)
        if (!modalidad.isNullOrEmpty()) {
            query = query.whereEqualTo("modalidad", modalidad)
        }

        // Limitar resultados
        if (limite != null && limite > 0) {
            query = query.limit(limite.toLong())
        }

        return ejecutarConsulta(query)
    }

    // ==========================================
    // 2. OBTENER ÚLTIMOS INSCRITOS (Top N)
    // ==========================================
    // Como tu modelo no tiene "rating", ordenamos por fecha de inscripción
    suspend fun obtenerUltimosInscritos(limit: Int): List<Restaurant> {
        val query = collectionRef
            .orderBy("fechaInscripcion", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        return ejecutarConsulta(query)
    }

    // ==========================================
    // 3. AUTOCOMPLETE (Buscar por nombre)
    // ==========================================
    suspend fun nombreEmpiezaPor(prefijo: String): List<Restaurant> {
        val query = collectionRef
            .orderBy("nombre")
            .startAt(prefijo)
            .endAt(prefijo + "\uf8ff")

        return ejecutarConsulta(query)
    }

    // ==========================================
    // HELPER (Método genérico privado)
    // ==========================================
    private suspend fun ejecutarConsulta(query: Query): List<Restaurant> {
        return try {
            val snapshot = query.get().await()

            snapshot.toObjects(Restaurant::class.java)
        } catch (e: Exception) {

            emptyList()
        }
    }
}