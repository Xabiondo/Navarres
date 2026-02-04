package com.example.navarres.model.data

data class OwnerRequest(
    val userId: String = "",
    val userEmail: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val mensaje: String = "",
    val estado: String = "pendiente", // pendiente, aceptada, rechazada
    val fecha: Long = System.currentTimeMillis()
)
