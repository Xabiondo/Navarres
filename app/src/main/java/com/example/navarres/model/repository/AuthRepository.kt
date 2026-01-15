package com.example.navarres.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    // Instancia de Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Función para Registrarse
    // Devuelve un Result que puede ser Éxito (con el usuario) o Fallo (con la excepción)
    suspend fun register(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            // .await() convierte la tarea de Firebase en una corutina (espera sin bloquear)
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
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