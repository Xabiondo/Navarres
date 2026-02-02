package com.example.navarres.model.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Comentario(
    @DocumentId
    val id: String = "",

    val restaurantId: String = "", // Se vinculará con Restaurant.id
    val userId: String = "",       // Se vinculará con User.uid


    val userName: String = "",     // Se rellena con User.displayName
    val userPhotoUrl: String? = null, // Se rellena con User.photoUrl

    val text: String = "",
    val rating: Int = 0,
    val date: Date = Date(),
    val likes: Int = 0,
    val parentId: String? = null
)