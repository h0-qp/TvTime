package com.example.data.firebase

data class Comment(
    val id: String = "",
    val mediaId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String? = null,
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L,
    val isGif: Boolean = false
)
