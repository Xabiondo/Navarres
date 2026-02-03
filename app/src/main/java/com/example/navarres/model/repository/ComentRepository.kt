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
    suspend fun toggleLike(comentarioId: String, uid: String, yaDioLike: Boolean) {
        try {
            val docRef = collectionRef.document(comentarioId)

            if (yaDioLike) {
                // Si ya dio like, lo quitamos (Dislike)
                docRef.update("likedBy", FieldValue.arrayRemove(uid)).await()
            } else {
                // Si no ha dado like, lo agregamos (Like)
                docRef.update("likedBy", FieldValue.arrayUnion(uid)).await()
            }
        } catch (e: Exception) {
            Log.e("CommentRepo", "Error toggle like: ${e.message}")
        }
    }
}