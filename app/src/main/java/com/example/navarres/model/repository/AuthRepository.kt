package com.example.navarres.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    //Google obliga a usar el patrón singleton, cuando arrancas la aplicación se generá una instancia, y solo
    //puedes cogerla , no hacer otra .



    suspend fun register(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            // .await() lo que hace es delegar la función en un otro hilo,
            //y espera hasta que ese hilo termine, para continuar con la función
            //pero de mientrás , el hilo principal puede seguir por ejemplo, pintando la pantalla.

            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()

            //Este código usa funciones de la clase FirebaseAuth

            Log.d("AuthRepository", "Registro exitoso: ${authResult.user?.email}")
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registro", e)
            Result.failure(e)
        }
    }

    // Función para Login
    suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            //Más de lo mismo , usando await para asincornía.
            Log.d("AuthRepository", "Login exitoso: ${authResult.user?.email}")
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login", e)
            Result.failure(e)
        }
    }

    // Función para salir
    fun logout() {
        auth.signOut()
    }

    // Obtener usuario actual (si ya está logueado de antes)
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}