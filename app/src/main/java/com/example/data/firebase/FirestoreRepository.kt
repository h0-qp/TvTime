package com.example.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class FirestoreMediaItem(
    val id: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val mediaType: String = "", // "tv" or "movie"
    var watched: Boolean = false,
    val addedAt: Long = 0L,
    val watchedEpisodes: List<String> = emptyList()
)


data class WatchlistShow(
    val showId: String = "",
    val addedAt: Long = 0L
)

data class WatchedEpisode(
    val showId: String = "",
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0,
    val watchedAt: Long = 0L
)

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Add or Update Media
    suspend fun addOrUpdateMedia(item: FirestoreMediaItem): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("المستخدم غير مسجل الدخول"))
        return try {
            db.collection("users").document(uid)
                .collection("media")
                .document(item.id.toString())
                .set(item)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove Media
    suspend fun removeMedia(mediaId: Int): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("المستخدم غير مسجل الدخول"))
        return try {
            db.collection("users").document(uid)
                .collection("media")
                .document(mediaId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get a specific item by ID
    suspend fun getMediaItem(mediaId: Int): FirestoreMediaItem? {
        val uid = currentUserId ?: return null
        return try {
            val doc = db.collection("users").document(uid)
                .collection("media")
                .document(mediaId.toString())
                .get()
                .await()
            doc.toObject(FirestoreMediaItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Observe all media for the current user
    
    fun observeWatchlistShows(): Flow<List<WatchlistShow>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid)
            .collection("watchlist_shows")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(WatchlistShow::class.java) }
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeWatchedEpisodes(): Flow<List<WatchedEpisode>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid)
            .collection("watched_episodes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(WatchedEpisode::class.java) }
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addTvShowToWatchlist(showId: String) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).collection("watchlist_shows").document(showId)
            .set(WatchlistShow(showId, System.currentTimeMillis())).await()
    }

    suspend fun removeTvShowFromWatchlist(showId: String) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).collection("watchlist_shows").document(showId).delete().await()
    }

    suspend fun markEpisodeWatched(showId: String, seasonNumber: Int, episodeNumber: Int) {
        val uid = currentUserId ?: return
        val docId = "${showId}_S${seasonNumber}E${episodeNumber}"
        db.collection("users").document(uid).collection("watched_episodes").document(docId)
            .set(WatchedEpisode(showId, seasonNumber, episodeNumber, System.currentTimeMillis())).await()
            
        // Also update the FirestoreMediaItem's watchedEpisodes list
        try {
            val mediaRef = db.collection("users").document(uid).collection("media").document(showId)
            val snapshot = mediaRef.get().await()
            if (snapshot.exists()) {
                val item = snapshot.toObject(FirestoreMediaItem::class.java)
                if (item != null) {
                    val currentList = item.watchedEpisodes.toMutableList()
                    val epKey = "S${seasonNumber}E${episodeNumber}"
                    if (!currentList.contains(epKey)) {
                        currentList.add(epKey)
                        mediaRef.update("watchedEpisodes", currentList).await()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markEpisodeUnwatched(showId: String, seasonNumber: Int, episodeNumber: Int) {
        val uid = currentUserId ?: return
        val docId = "${showId}_S${seasonNumber}E${episodeNumber}"
        db.collection("users").document(uid).collection("watched_episodes").document(docId).delete().await()
        
        // Also update the FirestoreMediaItem's watchedEpisodes list
        try {
            val mediaRef = db.collection("users").document(uid).collection("media").document(showId)
            val snapshot = mediaRef.get().await()
            if (snapshot.exists()) {
                val item = snapshot.toObject(FirestoreMediaItem::class.java)
                if (item != null) {
                    val currentList = item.watchedEpisodes.toMutableList()
                    val epKey = "S${seasonNumber}E${episodeNumber}"
                    if (currentList.contains(epKey)) {
                        currentList.remove(epKey)
                        mediaRef.update("watchedEpisodes", currentList).await()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun observeUserMedia(): Flow<List<FirestoreMediaItem>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid)
            .collection("media")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(FirestoreMediaItem::class.java) }
                    trySend(items)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun markEpisodesWatchedBatch(showId: String, showTitle: String, posterPath: String?, episodes: List<Pair<Int, Int>>) {
        val uid = currentUserId ?: return
        if (episodes.isEmpty()) return
        
        try {
            val batch = db.batch()
            
            // 1. Mark each episode as watched
            for ((seasonNumber, episodeNumber) in episodes) {
                val docId = "${showId}_S${seasonNumber}E${episodeNumber}"
                val docRef = db.collection("users").document(uid).collection("watched_episodes").document(docId)
                batch.set(docRef, WatchedEpisode(showId, seasonNumber, episodeNumber, System.currentTimeMillis()))
            }
            
            // 2. Fetch or update media item
            val mediaRef = db.collection("users").document(uid).collection("media").document(showId)
            val snapshot = mediaRef.get().await()
            
            val newEpisodeKeys = episodes.map { "S${it.first}E${it.second}" }
            
            if (snapshot.exists()) {
                val item = snapshot.toObject(FirestoreMediaItem::class.java)
                if (item != null) {
                    val currentList = item.watchedEpisodes.toMutableList()
                    var changed = false
                    for (key in newEpisodeKeys) {
                        if (!currentList.contains(key)) {
                            currentList.add(key)
                            changed = true
                        }
                    }
                    if (changed) {
                        batch.update(mediaRef, "watchedEpisodes", currentList)
                    }
                }
            } else {
                val newItem = FirestoreMediaItem(
                    id = showId.toIntOrNull() ?: 0,
                    title = showTitle,
                    posterPath = posterPath,
                    mediaType = "tv",
                    watched = false,
                    watchedEpisodes = newEpisodeKeys,
                    addedAt = System.currentTimeMillis()
                )
                batch.set(mediaRef, newItem)
            }
            
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
