package com.example.navarres.model.data

data class Restaurant(
    val nombre: String = "",
    val categoria: String = "",     // Ej: "Restaurante", "Asador"
    val modalidad: String = "",
    val direccion: String = "",
    val localidad: String = "",
    val municipio: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val foto: String = "",          // URL de la imagen
    val especialidad: List<String> = emptyList(),

    // --- CAMPOS NUEVOS ---
    val telefono: String = "",      // Ej: "948599106"
    val precio: String = "",        // Ej: "20-30 €"
    val valoracion: Double = 0.0    // Ej: 4.4
)