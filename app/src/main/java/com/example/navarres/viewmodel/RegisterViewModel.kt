package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Agregamos passwordStrength (0.0f a 1.0f) y passwordFeedback (mensaje)
data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val passwordStrength: Float = 0f, // 0.0 vacio, 1.0 segura
    val passwordFeedback: String = "" // Texto que dice qué falta
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    // Aquí está la MAGIA en tiempo real
    fun onPasswordChange(pass: String) {
        val (strength, feedback) = calculatePasswordStrength(pass)

        _uiState.update {
            it.copy(
                password = pass,
                error = null,
                passwordStrength = strength,
                passwordFeedback = feedback
            )
        }
    }

    private fun calculatePasswordStrength(pass: String): Pair<Float, String> {
        if (pass.isEmpty()) return 0f to ""

        var score = 0
        val missingRequirements = mutableListOf<String>()

        // 1. Mínimo 8 caracteres
        if (pass.length >= 8) score++ else missingRequirements.add("8+ caracteres")

        // 2. Al menos 1 mayúscula
        if (pass.any { it.isUpperCase() }) score++ else missingRequirements.add("1 mayúscula")

        // 3. Al menos 1 número
        if (pass.any { it.isDigit() }) score++ else missingRequirements.add("1 número")

        // 4. Al menos 1 símbolo (cualquier cosa que no sea letra ni número)
        if (pass.any { !it.isLetterOrDigit() }) score++ else missingRequirements.add("1 símbolo")

        // Calculamos porcentaje (4 requisitos = 0.25 cada uno)
        val strength = score / 4f

        val feedback = if (missingRequirements.isEmpty()) {
            "¡Contraseña segura!"
        } else {
            "Falta: ${missingRequirements.joinToString(", ")}"
        }

        return strength to feedback
    }

    fun register() {
        val currentState = _uiState.value

        // Validación estricta: Si la fuerza no es 1.0 (100%), no dejamos registrar
        if (currentState.passwordStrength < 1.0f) {
            _uiState.update { it.copy(error = "La contraseña no es segura. ${currentState.passwordFeedback}") }
            return
        }

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(error = "El email no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val authResult = authRepository.register(currentState.email, currentState.password)

            if (authResult.isSuccess) {
                val firebaseUser = authResult.getOrNull()
                if (firebaseUser != null) {
                    try {
                        userRepository.createUserProfile(firebaseUser.uid, currentState.email)
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, error = "Error guardando perfil: ${e.message}") }
                    }
                }
            } else {
                val exception = authResult.exceptionOrNull()
                _uiState.update { it.copy(isLoading = false, error = exception?.message ?: "Error desconocido") }
            }
        }
    }

    fun resetState() { _uiState.update { RegisterUiState() } }
}