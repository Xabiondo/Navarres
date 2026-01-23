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

// Estado de la UI
data class ProfileUiState(
    val photoUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// CLASE CORRECTA: ProfileViewModel
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // 1. Email del usuario
    private val _userEmail = MutableStateFlow(authRepository.getCurrentUser()?.email ?: "Usuario")
    val userEmail = _userEmail.asStateFlow()

    // 2. Estado de la foto
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser?.photoUrl != null) {
            _uiState.update { it.copy(photoUrl = currentUser.photoUrl.toString()) }
        }
    }

    // Lógica de subir foto
    fun onPhotoTaken(uri: Uri) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    val newUrl = userRepository.uploadProfilePicture(currentUser.uid, uri)
                    _uiState.update { it.copy(isLoading = false, photoUrl = newUrl) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}