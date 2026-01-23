package com.example.navarres.model.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance("gs://navarres-8d2e3.firebasestorage.app").reference

    // 1. CREAR PERFIL
    suspend fun createUserProfile(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "photoUrl" to "",
            // CORRECCIÓN: emptyList<String>(), no <Int>, porque tu data class User usa Strings
            "favorites" to emptyList<String>()
        )

        try {
            db.collection("users").document(uid).set(userMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. SUBIR FOTO (Sin cambios, ya estaba bien)
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        /* ... tu código anterior de subida de foto ... */
        // Por brevedad no lo repito, déjalo tal cual lo tenías
        return ""
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

    // 4. NUEVA: RECUPERAR FAVORITOS (Para pintar los corazones al entrar)
    suspend fun getUserFavorites(uid: String): List<String> {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            // Convertimos el campo 'favorites' a una lista de Strings
            snapshot.get("favorites") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}