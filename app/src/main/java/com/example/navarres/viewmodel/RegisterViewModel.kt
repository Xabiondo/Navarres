package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email, error = null) } }
    fun onPasswordChange(pass: String) { _uiState.update { it.copy(password = pass, error = null) } }

    fun register() {
        val currentState = _uiState.value
        // Validaciones básicas
        if (currentState.email.isBlank() || currentState.password.length < 6) {
            _uiState.update { it.copy(error = "Datos inválidos (pass min 6 chars)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // PASO 1: Crear usuario en Auth
            val authResult = authRepository.register(currentState.email, currentState.password)

            if (authResult.isSuccess) {
                val firebaseUser = authResult.getOrNull()
                if (firebaseUser != null) {
                    try {
                        // PASO 2: Crear perfil en Firestore
                        // Usamos el UID que nos dio Auth
                        userRepository.createUserProfile(firebaseUser.uid, currentState.email)

                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } catch (e: Exception) {
                        // Si falla Firestore, podríamos querer borrar el usuario de Auth o mostrar error
                        _uiState.update { it.copy(isLoading = false, error = "Cuenta creada, pero falló perfil: ${e.message}") }
                    }
                }
            } else {
                // Falló Auth (ej: email ya existe)
                val exception = authResult.exceptionOrNull()
                _uiState.update { it.copy(isLoading = false, error = exception?.message ?: "Error desconocido") }
            }
        }
    }

    fun resetState() { _uiState.update { RegisterUiState() } }
}