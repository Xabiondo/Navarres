package com.example.navarres.model.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private val storageRef = FirebaseStorage.getInstance("gs://navarres-8d2e3.firebasestorage.app").reference
    // Esta función crea el documento inicial en Firestore
    suspend fun createUserProfile(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "photoUrl" to "", // Vacío al principio
            "favorites" to emptyList<Int>() // Lista vacía para empezar
        )


        try {
            db.collection("users").document(uid).set(userMap).await()
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        try {
            // 1. Definir dónde se guardará en Storage: "profile_images/USER_ID.jpg"
            val imageRef = storageRef.child("profile_images/$uid.jpg")

            // 2. Subir el archivo
            imageRef.putFile(imageUri).await()

            // 3. Obtener la URL de descarga pública
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // 4. Actualizar el documento del usuario en Firestore con esa URL
            db.collection("users").document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            // Devolvemos la URL por si la UI la necesita al instante
            return downloadUrl

        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Lanzamos el error para que el ViewModel lo maneje
        }
    }




}