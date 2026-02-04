package com.example.navarres.model.data

import com.google.firebase.firestore.DocumentId

data class Restaurant(
    @DocumentId
    val id: String = "", // La anotación @DocumentId rellena esto automático
    val nombre: String = "",
    val categoria: String = "",
    val modalidad: String = "",
    val direccion: String = "",
    val localidad: String = "",
    val municipio: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val foto: String = "",
    val especialidad: List<String> = emptyList(),
    val precio: String = "",
    val telefono: String = "",
    val valoracion: Double = 0.0,
    val horarios: Map<String, String> = emptyMap(),
    val rutaCarta: String = "",
    val ownerId: String = "" // Importante para la función de reclamar negocio
)