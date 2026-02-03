package com.example.navarres.model.repository

import android.net.Uri
import android.util.Log
import com.example.navarres.model.data.OwnerRequest
import com.example.navarres.model.data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
// --- IMPORTS NUEVOS PARA TIEMPO REAL ---
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    // Asegúrate de que esta URL del bucket sea correcta (la que sale en tu consola de Firebase)
    private val storageRef = FirebaseStorage.getInstance("gs://navarres-8d2e3.firebasestorage.app").reference

    // 1. CREAR PERFIL
    suspend fun createUserProfile(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "photoUrl" to "",
            "favorites" to emptyList<String>(),
            "bio" to "",
            "city" to "",
            "isEmailPublic" to false
        )

        try {
            // Usamos set con merge por seguridad, para no borrar si ya existe
            db.collection("users").document(uid).set(userMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. SUBIR FOTO
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        try {
            val imageRef = storageRef.child("profile_images/$uid.jpg")
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            db.collection("users").document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            return downloadUrl

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // 3. LOGICA DE DAR LIKE / DISLIKE
    suspend fun toggleFavorite(uid: String, restaurantId: String, isAdding: Boolean) {
        try {
            val userRef = db.collection("users").document(uid)
            if (isAdding) {
                userRef.update("favorites", FieldValue.arrayUnion(restaurantId)).await()
            } else {
                userRef.update("favorites", FieldValue.arrayRemove(restaurantId)).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 4. RECUPERAR FAVORITOS (Versión estática, útil para comprobaciones puntuales)
    suspend fun getUserFavorites(uid: String): List<String> {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.get("favorites") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 5. ACTUALIZAR CAMPO GENÉRICO
    suspend fun updateUserField(uid: String, fieldName: String, value: Any) {
        try {
            db.collection("users").document(uid)
                .update(fieldName, value)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 6. OBTENER USUARIO (Versión estática)
    suspend fun getUser(uid: String): User? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // 7. OBTENER USUARIO EN TIEMPO REAL (NUEVA FUNCIÓN ⭐)
    // Esta función mantiene una conexión abierta y avisa cada vez que cambia algo
    fun getUserFlow(uid: String): Flow<User> = callbackFlow {
        val docRef = db.collection("users").document(uid)

        // Nos "suscribimos" a cambios
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Cerramos si hay error de conexión
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    trySend(user) // ¡Enviamos el dato nuevo!
                }
            }
        }

        // Esto se ejecuta cuando dejamos de escuchar (al cerrar la app o pantalla)
        awaitClose { subscription.remove() }
    }

    // En UserRepository.kt
// Añade esto a tu UserRepository.kt
    suspend fun enviarSolicitudDueno(
        uid: String,
        email: String,
        restauranteId: String,
        nombreRest: String,
        mensaje: String
    ): Boolean {
        return try {
            val solicitud = hashMapOf(
                "uid" to uid,
                "email" to email,
                "restauranteId" to restauranteId,
                "nombreRestaurante" to nombreRest,
                "mensaje" to mensaje,
                "fecha" to System.currentTimeMillis(),
                "estado" to "pendiente"
            )
            db.collection("solicitudes_dueño").add(solicitud).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // En UserRepository.kt
    suspend fun enviarSolicitudGenerica(datos: Map<String, Any>): Boolean {
        return try {
            // CAMBIAMOS EL NOMBRE AQUÍ PARA QUE COINCIDA CON LA EXTENSIÓN
            db.collection("solicitudes_verificacion")
                .add(datos)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al enviar: ${e.message}")
            false
        }
    }


}