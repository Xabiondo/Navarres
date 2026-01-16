package com.example.navarres.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var authError by mutableStateOf<String?>(null)
    var currentUser by mutableStateOf<FirebaseUser?>(repository.getCurrentUser())

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            authError = null
            val result = repository.login(email, pass)
            if (result.isSuccess) {
                currentUser = result.getOrNull()
                onSuccess()
            } else {
                authError = "Error al entrar: Credenciales incorrectas"
            }
            isLoading = false
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            authError = null
            val result = repository.register(email, pass)
            if (result.isSuccess) {
                currentUser = result.getOrNull()
                onSuccess()
            } else {
                authError = "Error al crear cuenta: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    // Dentro de AuthViewModel.kt
    fun logout() {
        repository.logout()
        currentUser = null // Al ponerlo a null, la MainActivity detectará el cambio y volverá al Login
    }
}