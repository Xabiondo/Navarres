package com.example.navarres.model.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    // Esta función crea el documento inicial en Firestore
    suspend fun createUserProfile(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "photoUrl" to "", // Vacío al principio
            "favorites" to emptyList<Int>() // Lista vacía para empezar
        )

        // Crea el documento en la colección "users" con el ID del usuario
        try {
            db.collection("users").document(uid).set(userMap).await()
        } catch (e: Exception) {
            // Manejar el error (logs, etc)
            e.printStackTrace()
        }
    }
}