package com.example.navarres.model.data

enum class Idioma { ES, EN, EU }

object Diccionario {
    private val textos = mapOf(
        Idioma.ES to mapOf(
            "perfil" to "Perfil",
            "idioma" to "Cambiar Idioma",
            "llamar" to "Llamar",
            "editar" to "Gestionar Local",
            "euskera" to "Euskara"
        ),
        Idioma.EN to mapOf(
            "perfil" to "Profile",
            "idioma" to "Change Language",
            "llamar" to "Call",
            "editar" to "Manage Venue",
            "euskera" to "Basque"
        ),
        Idioma.EU to mapOf(
            "perfil" to "Profila",
            "idioma" to "Hizkuntza aldatu",
            "llamar" to "Deitu",
            "editar" to "Lokala kudeatu",
            "euskera" to "Euskara"
        )
    )

    fun obtener(clave: String, idioma: Idioma): String {
        return textos[idioma]?.get(clave) ?: clave
    }
}