package com.example.navarres.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.User
import com.example.navarres.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. AQUÍ DEFINIMOS LA CLASE QUE FALTABA
// Esta clase agrupa los estados visuales (carga, foto temporal)
data class ProfileUiState(
    val isLoading: Boolean = false,
    val photoUrl: String = "" // URL temporal si subimos foto
)

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    // 2. ESTADO DEL USUARIO (Datos de Base de Datos)
    private val _userProfile = MutableStateFlow(User())
    val userProfile = _userProfile.asStateFlow()

    // 3. ESTADO DE LA UI (Cargas, errores, etc.) -> ESTO ES LO QUE TE DABA ERROR
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            // Activamos carga
            _uiState.value = _uiState.value.copy(isLoading = true)

            val user = repository.getUser(uid)
            if (user != null) {
                _userProfile.value = user
            }

            // Desactivamos carga
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    // Funciones para guardar texto (Bio, Ciudad, etc.)
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

    // Función para subir foto
    fun onPhotoTaken(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // 1. Ponemos el loading a TRUE
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 2. Subimos foto al Storage y obtenemos la URL
                val downloadUrl = repository.uploadProfilePicture(uid, uri)

                // 3. Actualizamos el User local y la UI State
                _userProfile.value = _userProfile.value.copy(photoUrl = downloadUrl)
                _uiState.value = _uiState.value.copy(photoUrl = downloadUrl) // Actualizamos también aquí por si acaso

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 4. Pase lo que pase, quitamos el loading
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}