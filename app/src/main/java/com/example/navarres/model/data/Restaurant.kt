package com.example.navarres.model.data
data class Restaurant(
    val nombre: String = "",
    val categoria: String = "",
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
    val rutaCarta: String = ""
)