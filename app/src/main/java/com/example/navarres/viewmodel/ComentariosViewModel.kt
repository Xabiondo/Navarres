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

data class CommentThread(
    val parent: Comentario,
    val replies: List<Comentario>
)

class ComentariosViewModel : ViewModel() {

    private val repository = CommentRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _threads = MutableStateFlow<List<CommentThread>>(emptyList())
    val threads = _threads.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun cargarComentarios(restauranteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val listaCruda = repository.obtenerComentariosPorRestaurante(restauranteId)
            _threads.value = agruparComentarios(listaCruda)
            _isLoading.value = false
        }
    }

    private fun agruparComentarios(lista: List<Comentario>): List<CommentThread> {
        val padres = lista.filter { it.parentId == null }.sortedByDescending { it.date }
        val respuestasMap = lista.filter { it.parentId != null }.groupBy { it.parentId }

        return padres.map { padre ->
            val hijos = respuestasMap[padre.id] ?: emptyList()
            CommentThread(parent = padre, replies = hijos.sortedBy { it.date })
        }
    }

    // --- CAMBIO AQUÍ: Añadido parámetro onSuccess ---
    fun enviarComentario(
        restauranteId: String,
        textoInput: String,
        valoracionInput: Int,
        parentId: String? = null,
        onSuccess: () -> Unit = {} // Callback para avisar cuando termine
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
                parentId = parentId,
                likedBy = emptyList()
            )

            // Esperamos a que el repositorio confirme el guardado
            val exito = repository.agregarComentario(nuevoComentario)

            if (exito) {
                // 1. Recargamos la lista local
                cargarComentarios(restauranteId)
                // 2. EJECUTAMOS EL CALLBACK AHORA (y no antes)
                onSuccess()
            }
            _isLoading.value = false
        }
    }

    fun toggleLike(comentario: Comentario) {
        val uid = auth.currentUser?.uid ?: return
        val yaDioLike = comentario.likedBy.contains(uid)
        val nuevaListaLikes = if (yaDioLike) comentario.likedBy - uid else comentario.likedBy + uid
        val comentarioActualizado = comentario.copy(likedBy = nuevaListaLikes)

        val listaActual = _threads.value.toMutableList()
        val indexPadre = listaActual.indexOfFirst { it.parent.id == comentario.id }

        if (indexPadre != -1) {
            val hiloAntiguo = listaActual[indexPadre]
            listaActual[indexPadre] = hiloAntiguo.copy(parent = comentarioActualizado)
        } else {
            for (i in listaActual.indices) {
                val hilo = listaActual[i]
                val indexHijo = hilo.replies.indexOfFirst { it.id == comentario.id }
                if (indexHijo != -1) {
                    val hijosMutable = hilo.replies.toMutableList()
                    hijosMutable[indexHijo] = comentarioActualizado
                    listaActual[i] = hilo.copy(replies = hijosMutable)
                    break
                }
            }
        }
        _threads.value = listaActual

        viewModelScope.launch {
            repository.toggleLike(comentario.id, uid, yaDioLike)
        }
    }
}