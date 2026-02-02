package com.example.navarres.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.User
import com.example.navarres.model.data.Restaurant
import com.example.navarres.model.repository.UserRepository
import com.example.navarres.model.repository.RestaurantRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val photoUrl: String = ""
)

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val restRepo = RestaurantRepository()

    private val _userProfile = MutableStateFlow(User())
    val userProfile = _userProfile.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    // --- NUEVO ESTADO PARA BÚSQUEDA ---
    private val _busquedaRestaurantes = MutableStateFlow<List<Restaurant>>(emptyList())
    val busquedaRestaurantes = _busquedaRestaurantes.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getUserFlow(uid)
                .catch { e ->
                    Log.e("ProfileViewModel", "Error: ${e.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .collect { updatedUser ->
                    _userProfile.value = updatedUser
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

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

    // --- MÉTODOS DE BÚSQUEDA Y SOLICITUD ---
    fun buscarRestaurantes(query: String) {
        if (query.length < 2) {
            _busquedaRestaurantes.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val lista = restRepo.nombreEmpiezaPor(query)
                _busquedaRestaurantes.value = lista
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error buscando: ${e.message}")
            }
        }
    }

    fun enviarSolicitud(restId: String, restNombre: String, mensaje: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val email = _userProfile.value.email
        viewModelScope.launch {
            val success = repository.enviarSolicitudDueno(uid, email, restId, restNombre, mensaje)
            onResult(success)
        }
    }

    // Actualiza esta función en tu ViewModel
    fun enviarSolicitudExtensa(
        restId: String,
        restNombre: String,
        datos: Map<String, String>,
        onResult: (Boolean) -> Unit
    ) {
        val user = _userProfile.value
        viewModelScope.launch {
            // Combinamos los datos del usuario con los del formulario
            val solicitudCompleta = datos + mapOf(
                "userId" to user.uid,
                "userEmail" to user.email,
                "restaurantId" to restId,
                "restaurantNombre" to restNombre,
                "fecha" to java.util.Date().toString(),
                "estado" to "pendiente"
            )
            // Usamos el repositorio existente para enviar el mapa completo
            val success = repository.enviarSolicitudGenerica(solicitudCompleta)
            onResult(success)
        }
    }

    fun logout() {
        auth.signOut()
    }
}