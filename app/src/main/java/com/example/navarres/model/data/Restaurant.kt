package com.example.navarres.model.data


data class Restaurant(
    val id : Int ,
    val identificador : String ,
    val nombre : String ,
    val modalidad : String ,
    val catergoria : String ,
    val direccion : String ,
    val localidad : String ,
    val codigoPostal : String ,
    val municipio : String ,
    val subZona : String ,
    val especialidad : List<String> ,
    val fechaInscripcion : String

)