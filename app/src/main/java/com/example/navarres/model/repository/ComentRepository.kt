package com.example.navarres.model.repository

import android.util.Log
import com.example.navarres.model.data.Comentario
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommentRepository {

    private val db = Firebase.firestore
    private val collectionRef = db.collection("comentarios")

    /**
     * FUNCIÓN CLAVE: Devuelve un FLOW (un flujo de datos vivo).
     * Se ejecuta automáticamente cada vez que alguien escribe en la base de datos.
     */
    fun obtenerFlujoComentarios(restauranteId: String): Flow<List<Comentario>> = callbackFlow {
        val listener = collectionRef
            .whereEqualTo("restaurantId", restauranteId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommentRepo", "Error escuchando cambios: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.toObjects(Comentario::class.java)
                    trySend(lista) // Enviamos la nueva lista a la app
                }
            }

        // Se ejecuta cuando cierras la pantalla para limpiar memoria
        awaitClose { listener.remove() }
    }

    suspend fun agregarComentario(comentario: Comentario): Boolean {
        return try {
            collectionRef.add(comentario).await()
            true
        } catch (e: Exception) {
            Log.e("CommentRepo", "Error al guardar: ${e.message}")
            false
        }
    }

    suspend fun toggleLike(comentarioId: String, uid: String, yaDioLike: Boolean) {
        try {
            val docRef = collectionRef.document(comentarioId)
            if (yaDioLike) {
                docRef.update("likedBy", FieldValue.arrayRemove(uid)).await()
            } else {
                docRef.update("likedBy", FieldValue.arrayUnion(uid)).await()
            }
        } catch (e: Exception) {
            Log.e("CommentRepo", "Error toggle like: ${e.message}")
        }
    }
}