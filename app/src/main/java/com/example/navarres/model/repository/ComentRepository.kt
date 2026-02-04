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
     * Está función devuelve un flow, que es un objeto que se ejecuta cada vez
     * que un apersona escribe algo en la base de datos, asi coneguimos el efecto
     * de que los chats van en vivo
     */
    fun obtenerFlujoComentarios(restauranteId: String): Flow<List<Comentario>> = callbackFlow {
        val listener = collectionRef
            .whereEqualTo("restaurantId", restauranteId)
            .orderBy("date", Query.Direction.DESCENDING)

            //Básicamente aquí hemos creado un objeto que esta pendiente de cuando hay cambios en la base de datos.
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommentRepo", "Error escuchando cambios: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.toObjects(Comentario::class.java)
                    trySend(lista) // Esto envía los nuevos objetos, no se usa return porque entonces se sale de la función
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