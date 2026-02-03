package com.example.navarres.model.repository

import android.util.Log
import com.example.navarres.model.data.Comentario
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CommentRepository {

    private val db = Firebase.firestore
    private val collectionRef = db.collection("comentarios")

    // ... (obtenerComentariosPorRestaurante sigue IGUAL) ...
    suspend fun obtenerComentariosPorRestaurante(restauranteId: String): List<Comentario> {
        return try {
            val snapshot = collectionRef
                .whereEqualTo("restaurantId", restauranteId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Comentario::class.java)
        } catch (e: Exception) {
            Log.e("CommentRepo", "Error: ${e.message}")
            emptyList()
        }
    }

    // ... (agregarComentario sigue IGUAL) ...
    suspend fun agregarComentario(comentario: Comentario): Boolean {
        return try {
            collectionRef.add(comentario).await()
            true
        } catch (e: Exception) {
            Log.e("CommentRepo", "Error al guardar: ${e.message}")
            false
        }
    }

    // --- NUEVO: DAR LIKE ---
    suspend fun darLike(comentarioId: String) {
        try {
            // Incrementa en 1 el campo "likes" directamente en la base de datos
            collectionRef.document(comentarioId)
                .update("likes", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Log.e("CommentRepo", "Error dando like: ${e.message}")
        }
    }
}