package com.example.navarres.viewmodel

import android.net.Uri
import android.util.Log // Importar Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.User
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch // IMPORTANTE: Añade este import
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val photoUrl: String = ""
)

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableStateFlow(User())
    val userProfile = _userProfile.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getUserFlow(uid)
                // --- AQUÍ ESTÁ EL ARREGLO ---
                .catch { e ->
                    // Si ocurre un error (ej. PERMISSION_DENIED al cerrar sesión),
                    // entramos aquí en lugar de cerrar la app.
                    Log.e("ProfileViewModel", "Error escuchando cambios de usuario (probablemente logout): ${e.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                // -----------------------------
                .collect { updatedUser ->
                    _userProfile.value = updatedUser
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    // ... Resto de funciones (updateBio, updateCity, onPhotoTaken, logout) iguales ...

    fun updateBio(newBio: String) {
        val uid = auth.currentUser?.uid ?: return
        _userProfile.value = _userProfile.value.copy(bio = newBio)
        viewModelScope.launch { repository.updateUserField(uid, "bio", newBio) }
    }

    fun updateCity(newCity: String) {
        val uid = auth.currentUser?.uid ?: return
        _userProfile.value = _userProfile.value.copy(city = newCity)
        viewModelScope.launch { repository.updateUserField(uid, "city", newCity) }
    }

    fun updateEmailPrivacy(isPublic: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        _userProfile.value = _userProfile.value.copy(isEmailPublic = isPublic)
        viewModelScope.launch { repository.updateUserField(uid, "isEmailPublic", isPublic) }
    }

    fun onPhotoTaken(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val downloadUrl = repository.uploadProfilePicture(uid, uri)
                _uiState.value = _uiState.value.copy(photoUrl = downloadUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}