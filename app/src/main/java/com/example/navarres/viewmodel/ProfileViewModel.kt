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
// --- NUEVO MÉTODO PARA BUSCAR EN EL DIÁLOGO ---
    fun buscarRestaurantes(query: String) {
        if (query.isEmpty()) {
            _busquedaRestaurantes.value = emptyList()
            return
        }

        // Normalizamos la búsqueda: pasamos la primera a Mayúscula por si acaso
        val formattedQuery = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                // LOG DE CONTROL: Mira esto en el Logcat
                android.util.Log.d("NAV_DEBUG", "Buscando en Firebase: '$formattedQuery' (Original: '$query')")

                db.collection("restaurantes")
                    .orderBy("nombre") // IMPORTANTE: Para que el rango funcione bien
                    .startAt(formattedQuery)
                    .endAt(formattedQuery + "\uf8ff")
                    .limit(5)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val lista = snapshot.documents.mapNotNull { doc ->
                            val rest = doc.toObject(Restaurant::class.java)
                            rest?.copy(id = doc.id)
                        }
                        android.util.Log.d("NAV_DEBUG", "Encontrados: ${lista.size}")
                        _busquedaRestaurantes.value = lista
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("NAV_DEBUG", "Fallo en búsqueda: ${e.message}")
                    }
            } catch (e: Exception) {
                android.util.Log.e("NAV_DEBUG", "Excepción: ${e.message}")
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

    // En ProfileViewModel.kt
    fun enviarSolicitudDossierEmail(
        restId: String,
        restNombre: String,
        datosFormulario: Map<String, String>,
        onResult: (Boolean) -> Unit
    ) {
        val user = _userProfile.value

        // 1. BLINDAJE DE SEGURIDAD: Si falta el ID del restaurante o el usuario, abortamos.
        if (restId.isBlank()) {
            Log.e("NAV_ERROR", "Error: El ID del restaurante llegó vacío al ViewModel")
            onResult(false)
            return
        }

        if (user == null || user.uid.isEmpty()) {
            Log.e("NAV_ERROR", "Error: Usuario no identificado")
            onResult(false)
            return
        }

        val functionUrl = "https://us-central1-navarres-8d2e3.cloudfunctions.net/aprobarDuenio"

        // 2. CONSTRUCCIÓN LIMPIA: Usamos variables intermedias para evitar errores de símbolos
        val userUid = user.uid
        val userEmail = user.email

        val approvalLink = "${functionUrl}?uid=${userUid}&restId=${restId}&restNombre=${restNombre}&email=${userEmail}"

        viewModelScope.launch {
            try {
                val solicitudData = hashMapOf(
                    "to" to "ivantorrano04@gmail.com",
                    "message" to hashMapOf(
                        "subject" to "⚠️ RECLAMACIÓN: $restNombre",
                        "html" to """
                <div style="font-family: sans-serif; border: 1px solid #e0e0e0; padding: 20px; border-radius: 12px; max-width: 500px;">
                    <div style="background: #D32F2F; padding: 15px; color: white; border-radius: 8px 8px 0 0; text-align: center;">
                        <h2 style="margin: 0;">Solicitud de Verificación</h2>
                    </div>
                    
                    <div style="padding: 20px; border: 1px solid #eee; border-top: none; border-radius: 0 0 8px 8px;">
                        <p>El usuario <b>$userEmail</b> quiere gestionar:</p>
                        <h1 style="color: #1a1a1a; font-size: 22px;">$restNombre</h1>
                        
                        <div style="background: #f9f9f9; padding: 15px; border-radius: 8px; margin: 20px 0; font-size: 14px;">
                            <p style="margin: 5px 0;"><strong>CIF:</strong> ${datosFormulario["cif"] ?: "No indicado"}</p>
                            <p style="margin: 5px 0;"><strong>Cargo:</strong> ${datosFormulario["cargo"] ?: "No indicado"}</p>
                            <p style="margin: 5px 0;"><strong>Teléfono:</strong> ${datosFormulario["telefono"] ?: "No indicado"}</p>
                        </div>

                        <div style="margin-top: 25px;">
                            <a href="$approvalLink" 
                               style="background: #2E7D32; color: white; padding: 15px; text-decoration: none; border-radius: 8px; font-weight: bold; display: block; text-align: center; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                               ✅ APROBAR Y ASIGNAR DUEÑO
                            </a>
                            
                            <a href="mailto:$userEmail?subject=Navarres: Solicitud Denegada" 
                               style="display: block; text-align: center; margin-top: 15px; color: #D32F2F; text-decoration: none; font-size: 14px;">
                               ❌ Denegar manualmente
                            </a>
                        </div>
                    </div>
                </div>
            """.trimIndent()
                    ),
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                val success = repository.enviarSolicitudGenerica(solicitudData)
                onResult(success)
            } catch (e: Exception) {
                Log.e("NAV_ERROR", "Error al crear documento: ${e.message}")
                onResult(false)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}