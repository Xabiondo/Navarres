package com.example.navarres.model.repository

import android.net.Uri
import android.util.Log
import com.example.navarres.model.data.OwnerRequest
import com.example.navarres.model.data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private val storageRef = FirebaseStorage.getInstance("gs://navarres-8d2e3.firebasestorage.app").reference


    suspend fun createUserProfile(uid: String, email: String, displayName: String = "") {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to "",
            "favorites" to emptyList<String>(),
            "bio" to "",
            "city" to "",
            "isEmailPublic" to false
        )
        /***
         * Esta función coge todos los campos del usuario, que se ha registrado en firebase Auth, y crea un documento
         * Usuario en la base de datos
         */


        try {
            db.collection("users").document(uid).set(userMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        try {
            val imageRef = storageRef.child("profile_images/$uid.jpg")
            //La ruta donde se guarda en firebase storage las fotos que suban los usuarios.
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            db.collection("users").document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            return downloadUrl

            //Devuelve un string,que es la url de la foto del usuario.

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    suspend fun updateUserField(uid: String, fieldName: String, value: Any) {
        try {
            db.collection("users").document(uid)
                .update(fieldName, value)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //Báscicamente, cambia todo lo que el usuario quiera
    }


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
        //Esta función añade o quita los restaurantes del usuario de favoritos, pero solo su id, para evitar
        //duplicar datos inecesarios
    }


    suspend fun getUserFavorites(uid: String): List<String> {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.get("favorites") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        //Devuevle la lista de los restautantes favoritos del usuario
    }


    fun getUserFlow(uid: String): Flow<User> = callbackFlow {
        val docRef = db.collection("users").document(uid)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) trySend(user)
            }
        }
        //Esta función lo que hace es darse cuenta de cuando hay un cambio en cualquier
        //dato del usuario, y la cambia automáticamente, sin necesidad de recargar la pantalla.
        //Además si la cambias desde otro dispositivo, también funciona

        awaitClose { subscription.remove() }
    }

    suspend fun getUser(uid: String): User? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }




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


    suspend fun enviarSolicitudGenerica(datos: Map<String, Any>): Boolean {
        return try {

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