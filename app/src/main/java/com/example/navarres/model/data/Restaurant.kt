package com.example.navarres.model.data

data class Restaurant(
    val nombre: String = "",
    val categoria: String = "", // El número de tenedores (ej: "3")
    val modalidad: String = "",
    val direccion: String = "",
    val localidad: String = "",
    val municipio: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val foto: String = "", // URL de la imagen en Firebase Storage o web
    val especialidad: List<String> = emptyList()
)