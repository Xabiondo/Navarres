package com.example.navarres.model.data

data class User(
    val uid: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val favorites: List<String> = emptyList(),
    val bio: String = "",
    val city: String = "",
    val isEmailPublic: Boolean = false,
    val ownerOf: String? = null // <--- DEBE ESTAR AQUÍ
)