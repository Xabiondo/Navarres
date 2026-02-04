package com.example.navarres.model.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Comentario(
    @DocumentId
    val id: String = "",

    val restaurantId: String = "",
    val userId: String = "",


    val userName: String = "",
    val userPhotoUrl: String? = null,

    val text: String = "",
    val rating: Int = 0,
    val date: Date = Date(),
    val likes: Int = 0,
    val parentId: String? = null ,
    val likedBy: List<String> = emptyList(),
){

    val likesCount: Int
        get() = likedBy.size
}