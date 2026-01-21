package com.example.navarres.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.repository.AuthRepository
import com.example.navarres.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Estado de la UI: Define qué datos necesita la pantalla para dibujarse
data class ProfileUiState(
    val photoUrl: String = "",        // La URL de la foto (si existe)
    val isLoading: Boolean = false,   // ¿Estamos subiendo algo?
    val error: String? = null         // Si algo sale mal
)

// 2. La Clase ViewModel
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado observable (la UI se "suscribe" a esto)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    // Inicialización: Intentamos cargar la foto si el usuario ya tiene una
    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            // Si el objeto usuario de Firebase ya tiene foto, la ponemos de inicio
            val currentPhoto = currentUser.photoUrl
            if (currentPhoto != null) {
                _uiState.update { it.copy(photoUrl = currentPhoto.toString()) }
            }
        }
    }

    // 3. FUNCIÓN PRINCIPAL: Recibe la URI de la cámara y la sube
    fun onPhotoTaken(uri: Uri) {
        val currentUser = authRepository.getCurrentUser()

        if (currentUser != null) {
            viewModelScope.launch {
                // A) Ponemos estado de carga (el spinner girando)
                _uiState.update { it.copy(isLoading = true, error = null) }

                try {
                    // B) Llamamos al repositorio (sube a Storage -> actualiza Firestore)
                    val newUrl = userRepository.uploadProfilePicture(currentUser.uid, uri)

                    // C) Si todo va bien, actualizamos la UI con la nueva URL
                    _uiState.update {
                        it.copy(isLoading = false, photoUrl = newUrl)
                    }

                } catch (e: Exception) {
                    // D) Si falla, mostramos el error y quitamos la carga
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error al subir: ${e.message}")
                    }
                }
            }
        } else {
            _uiState.update { it.copy(error = "No hay usuario identificado") }
        }
    }
}