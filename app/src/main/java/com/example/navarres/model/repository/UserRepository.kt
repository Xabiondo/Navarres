package com.example.navarres.model.repository

import android.net.Uri
import com.example.navarres.model.data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage

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

    // 2. SUBIR FOTO (CÓDIGO CORREGIDO Y RELLENADO)
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        try {
            // a) Referencia: carpeta "profile_images", nombre "UID.jpg"
            val imageRef = storageRef.child("profile_images/$uid.jpg")

            // b) Subir el archivo (putFile)
            imageRef.putFile(imageUri).await()

            // c) Obtener la URL pública de descarga
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // d) Guardar esa URL en el documento del usuario en Firestore
            db.collection("users").document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            return downloadUrl

        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Lanzamos el error para que el ViewModel sepa que falló
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

    // 4. RECUPERAR FAVORITOS
    suspend fun getUserFavorites(uid: String): List<String> {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.get("favorites") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 5. ACTUALIZAR CAMPO GENÉRICO (Bio, Ciudad, etc.)
    suspend fun updateUserField(uid: String, fieldName: String, value: Any) {
        try {
            db.collection("users").document(uid)
                .update(fieldName, value)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 6. OBTENER USUARIO COMPLETO
    suspend fun getUser(uid: String): User? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}