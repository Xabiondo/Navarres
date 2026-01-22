package com.example.navarres.model.data

data class Restaurant(
    val id: Int = 0,
    val identificador: String = "",
    val nombre: String = "",
    val modalidad: String = "",
    val categoria: String = "",
    val direccion: String = "",
    val localidad: String = "",
    val codigoPostal: String = "",
    val latitud : Double = 0.0,
    val longitud : Double = 0.0,
    val municipio: String = "",
    val subZona: String = "",
    val especialidad: List<String> = emptyList(),
    val fechaInscripcion: String = ""
)