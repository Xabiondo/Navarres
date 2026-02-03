package com.example.navarres.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navarres.model.data.Comentario
import com.example.navarres.model.repository.CommentRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
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

    private var listeningJob: Job? = null

    fun cargarComentarios(restauranteId: String) {
        listeningJob?.cancel() // Cancelamos escuchas anteriores

        listeningJob = viewModelScope.launch {
            _isLoading.value = true
            // NOS SUSCRIBIMOS AL FLUJO EN TIEMPO REAL
            repository.obtenerFlujoComentarios(restauranteId).collect { listaCruda ->
                _threads.value = agruparComentarios(listaCruda)
                _isLoading.value = false
            }
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

    fun enviarComentario(
        restauranteId: String,
        textoInput: String,
        valoracionInput: Int,
        parentId: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (textoInput.isBlank()) return

        viewModelScope.launch {
            // No necesitamos activar isLoading manual aquí porque el listener
            // de arriba detectará el nuevo comentario y actualizará la UI solo.
            val user = auth.currentUser
            val uid = user?.uid ?: "anonimo"
            var nombreFinal = user?.displayName ?: "Usuario"
            var fotoFinal = user?.photoUrl?.toString()

            try {
                val snapshot = Firebase.firestore.collection("users").document(uid).get().await()
                nombreFinal = snapshot.getString("displayName")?.takeIf { it.isNotBlank() } ?: nombreFinal
                fotoFinal = snapshot.getString("photoUrl")?.takeIf { it.isNotBlank() } ?: fotoFinal
            } catch (e: Exception) { /* Ignorar */ }

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

            val exito = repository.agregarComentario(nuevoComentario)
            if (exito) {
                onSuccess()
            }
        }
    }

    fun toggleLike(comentario: Comentario) {
        val uid = auth.currentUser?.uid ?: return
        val yaDioLike = comentario.likedBy.contains(uid)

        // Optimistic update para que se vea instantáneo
        val nuevaListaLikes = if (yaDioLike) comentario.likedBy - uid else comentario.likedBy + uid
        val comentarioActualizado = comentario.copy(likedBy = nuevaListaLikes)
        actualizarLocalmente(comentarioActualizado)

        viewModelScope.launch {
            repository.toggleLike(comentario.id, uid, yaDioLike)
        }
    }

    private fun actualizarLocalmente(comentario: Comentario) {
        val listaActual = _threads.value.toMutableList()
        val indexPadre = listaActual.indexOfFirst { it.parent.id == comentario.id }
        if (indexPadre != -1) {
            val hiloAntiguo = listaActual[indexPadre]
            listaActual[indexPadre] = hiloAntiguo.copy(parent = comentario)
        } else {
            for (i in listaActual.indices) {
                val hilo = listaActual[i]
                val indexHijo = hilo.replies.indexOfFirst { it.id == comentario.id }
                if (indexHijo != -1) {
                    val hijosMutable = hilo.replies.toMutableList()
                    hijosMutable[indexHijo] = comentario
                    listaActual[i] = hilo.copy(replies = hijosMutable)
                    break
                }
            }
        }
        _threads.value = listaActual
    }
}