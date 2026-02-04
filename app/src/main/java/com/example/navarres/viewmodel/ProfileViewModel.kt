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

        if (restId.isBlank() || user.uid.isEmpty()) {
            Log.e("NAV_ERROR", "Datos insuficientes: restId=$restId, userUid=${user.uid}")
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                // URL de tu Firebase Function
                val functionUrl = "https://us-central1-navarres-8d2e3.cloudfunctions.net/aprobarDuenio"

                // Construcción del enlace con todos los parámetros necesarios
                val approvalLink = "$functionUrl?uid=${user.uid}&restId=$restId&restNombre=${restNombre}&email=${user.email}"

                val emailHtml = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; padding: 40px 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                        <div style="background: #D32F2F; padding: 30px; text-align: center; color: white;">
                            <p style="text-transform: uppercase; letter-spacing: 2px; margin: 0; font-size: 12px; opacity: 0.9;">Nueva Reclamación</p>
                            <h1 style="margin: 10px 0 0 0; font-size: 26px;">${restNombre.uppercase()}</h1>
                        </div>
                        
                        <div style="padding: 40px;">
                            <p style="color: #555; font-size: 16px; line-height: 1.6;">Hola Administrador,</p>
                            <p style="color: #555; font-size: 16px; line-height: 1.6;">Se ha recibido una nueva solicitud para gestionar un establecimiento en la plataforma <strong>Navarres</strong>.</p>
                            
                            <div style="background: #f8f9fa; border-left: 4px solid #D32F2F; padding: 20px; margin: 25px 0;">
                                <p style="margin: 0 0 10px 0;"><strong>👤 Usuario:</strong> ${user.email}</p>
                                <p style="margin: 0 0 10px 0;"><strong>🆔 UID:</strong> ${user.uid}</p>
                                <p style="margin: 0 0 10px 0;"><strong>🏢 Cargo:</strong> ${datosFormulario["cargo"] ?: "N/A"}</p>
                                <p style="margin: 0 0 10px 0;"><strong>📄 CIF/NIF:</strong> ${datosFormulario["cif"] ?: "N/A"}</p>
                                <p style="margin: 0;"><strong>📞 Teléfono:</strong> ${datosFormulario["telefono"] ?: "N/A"}</p>
                            </div>

                            <p style="color: #e53935; font-size: 13px; font-style: italic; margin-top: 30px;">
                                Al hacer clic en el botón de abajo, se vinculará automáticamente este restaurante al usuario y se le otorgarán permisos de edición.
                            </p>

                            <a href="$approvalLink" 
                               style="display: block; background: #2E7D32; color: white; padding: 18px; text-decoration: none; border-radius: 10px; font-weight: bold; text-align: center; margin-top: 20px; font-size: 16px;">
                               ✅ APROBAR Y DAR ACCESO
                            </a>
                            
                            <hr style="border: 0; border-top: 1px solid #eee; margin: 40px 0 20px 0;">
                            <p style="text-align: center; font-size: 12px; color: #999;">
                                Si no reconoces esta solicitud o sospechas de fraude, simplemente ignora este correo o contacta con el usuario.
                            </p>
                        </div>
                    </div>
                </div>
            """.trimIndent()

                val emailData = hashMapOf(
                    "to" to "ivantorrano04@gmail.com",
                    "message" to hashMapOf(
                        "subject" to "🔔 SOLICITUD: $restNombre (${user.email})",
                        "html" to emailHtml
                    ),
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                val success = repository.enviarSolicitudGenerica(emailData)
                onResult(success)

            } catch (e: Exception) {
                Log.e("NAV_ERROR", "Error al procesar solicitud: ${e.message}")
                onResult(false)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}