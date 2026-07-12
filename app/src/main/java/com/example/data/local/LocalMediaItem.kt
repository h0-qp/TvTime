package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class LocalMediaItem(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String, // "tv" or "movie"
    val isWatched: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
