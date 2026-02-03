package com.example.navarres.model.data

data class RestaurantStats(
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val countsPerStar: Map<Int, Int> = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
)