package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE mediaType = :mediaType ORDER BY addedAt DESC")
    fun getMediaItemsByType(mediaType: String): Flow<List<LocalMediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: LocalMediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaItem(id: Int)

    @Query("UPDATE media_items SET isWatched = :isWatched WHERE id = :id")
    suspend fun updateWatchedStatus(id: Int, isWatched: Boolean)
}
