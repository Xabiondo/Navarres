package com.example.navarres.model.data

data class User(
    val uid: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val favorites: List<String> = emptyList()
)