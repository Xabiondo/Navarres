package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Comentario
import com.example.navarres.model.repository.CommentRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- CLASE AUXILIAR PARA AGRUPAR (Estilo YouTube) ---
data class CommentThread(
    val parent: Comentario,
    val replies: List<Comentario>
)

class ComentariosViewModel : ViewModel() {

    private val repository = CommentRepository()
    private val auth = FirebaseAuth.getInstance()

    // AHORA LA LISTA ES DE HILOS (Padre + Hijos), NO DE COMENTARIOS SUELTOS
    private val _threads = MutableStateFlow<List<CommentThread>>(emptyList())
    val threads = _threads.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun cargarComentarios(restauranteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val listaCruda = repository.obtenerComentariosPorRestaurante(restauranteId)

            // AGRUPAMOS AL ESTILO YOUTUBE
            val hilos = agruparComentarios(listaCruda)

            _threads.value = hilos
            _isLoading.value = false
        }
    }

    private fun agruparComentarios(lista: List<Comentario>): List<CommentThread> {
        // 1. Sacamos los padres (los que no tienen parentId)
        val padres = lista.filter { it.parentId == null }
            .sortedByDescending { it.date }

        // 2. Sacamos las respuestas y las agrupamos por el ID del padre
        val respuestasMap = lista.filter { it.parentId != null }
            .groupBy { it.parentId }

        // 3. Creamos los objetos CommentThread
        return padres.map { padre ->
            val hijos = respuestasMap[padre.id] ?: emptyList()
            CommentThread(
                parent = padre,
                replies = hijos.sortedBy { it.date } // Respuestas en orden cronológico
            )
        }
    }

    fun enviarComentario(
        restauranteId: String,
        textoInput: String,
        valoracionInput: Int,
        parentId: String? = null
    ) {
        if (textoInput.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            val user = auth.currentUser
            val uid = user?.uid ?: "anonimo"

            var nombreFinal = "Usuario Anónimo"
            var fotoFinal: String? = null

            try {
                val snapshot = Firebase.firestore.collection("users").document(uid).get().await()
                nombreFinal = snapshot.getString("displayName")?.takeIf { it.isNotBlank() } ?: user?.displayName ?: "Usuario"
                fotoFinal = snapshot.getString("photoUrl")?.takeIf { it.isNotBlank() } ?: user?.photoUrl?.toString()
            } catch (e: Exception) {
                nombreFinal = user?.displayName ?: "Usuario"
                fotoFinal = user?.photoUrl?.toString()
            }

            val nuevoComentario = Comentario(
                restaurantId = restauranteId,
                userId = uid,
                userName = nombreFinal,
                userPhotoUrl = fotoFinal,
                text = textoInput,
                rating = valoracionInput,
                parentId = parentId
            )

            val exito = repository.agregarComentario(nuevoComentario)

            if (exito) {
                cargarComentarios(restauranteId)
            }
            _isLoading.value = false
        }
    }

    fun darLike(comentario: Comentario) {
        // Actualización optimista: Buscamos el hilo y actualizamos el comentario dentro
        val listaActual = _threads.value.toMutableList()

        // Buscamos si es un padre
        val indexPadre = listaActual.indexOfFirst { it.parent.id == comentario.id }
        if (indexPadre != -1) {
            val hiloAntiguo = listaActual[indexPadre]
            val padreActualizado = comentario.copy(likes = comentario.likes + 1)
            listaActual[indexPadre] = hiloAntiguo.copy(parent = padreActualizado)
        } else {
            // Si no es padre, buscamos en los hijos de todos los hilos
            for (i in listaActual.indices) {
                val hilo = listaActual[i]
                val indexHijo = hilo.replies.indexOfFirst { it.id == comentario.id }
                if (indexHijo != -1) {
                    val hijosMutable = hilo.replies.toMutableList()
                    hijosMutable[indexHijo] = comentario.copy(likes = comentario.likes + 1)
                    listaActual[i] = hilo.copy(replies = hijosMutable)
                    break
                }
            }
        }

        _threads.value = listaActual

        viewModelScope.launch {
            repository.darLike(comentario.id)
        }
    }
}