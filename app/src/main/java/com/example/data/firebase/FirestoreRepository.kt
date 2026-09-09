package com.example.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

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

    fun getCurrentUser() = auth.currentUser

    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val uid = currentUserId ?: return
        db.collection("users").document(uid).set(profile, SetOptions.merge()).await()
    }

    fun observeUserMedia(): Flow<List<FirestoreMediaItem>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("media")
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

    suspend fun addOrUpdateMedia(item: FirestoreMediaItem): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.collection("users").document(uid).collection("media").document(item.id.toString()).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMedia(mediaId: Int): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.collection("users").document(uid).collection("media").document(mediaId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeWatchlistShows(): Flow<List<WatchlistShow>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("watchlist_shows")
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

    suspend fun addTvShowToWatchlist(showId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val show = WatchlistShow(showId = showId, addedAt = System.currentTimeMillis())
            db.collection("users").document(uid).collection("watchlist_shows").document(showId).set(show).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeTvShowFromWatchlist(showId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.collection("users").document(uid).collection("watchlist_shows").document(showId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeWatchedEpisodes(): Flow<List<WatchedEpisode>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("watched_episodes")
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

    suspend fun markEpisodeWatched(showId: String, seasonNumber: Int, episodeNumber: Int): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val episodeId = "${showId}_${seasonNumber}_${episodeNumber}"
            val episode = WatchedEpisode(showId, seasonNumber, episodeNumber, System.currentTimeMillis())
            db.collection("users").document(uid).collection("watched_episodes").document(episodeId).set(episode).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markEpisodeUnwatched(showId: String, seasonNumber: Int, episodeNumber: Int): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val episodeId = "${showId}_${seasonNumber}_${episodeNumber}"
            db.collection("users").document(uid).collection("watched_episodes").document(episodeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markEpisodesWatchedBatch(showId: String, showTitle: String, posterPath: String?, episodes: List<Pair<Int, Int>>): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val batch = db.batch()
            for (ep in episodes) {
                val episodeId = "${showId}_${ep.first}_${ep.second}"
                val docRef = db.collection("users").document(uid).collection("watched_episodes").document(episodeId)
                val episode = WatchedEpisode(showId, ep.first, ep.second, System.currentTimeMillis())
                batch.set(docRef, episode)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeCommentsForMedia(mediaId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("comments")
            .whereEqualTo("mediaId", mediaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        val comment = doc.toObject(Comment::class.java)
                        comment?.copy(id = doc.id)
                    }.sortedByDescending { it.timestamp }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: Comment, imageBytes: ByteArray? = null): Result<Unit> = suspendCoroutine { continuation ->
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                
                var inSampleSize = 1
                val reqWidth = 800
                val reqHeight = 800
                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                    val halfHeight: Int = options.outHeight / 2
                    val halfWidth: Int = options.outWidth / 2
                    while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                        inSampleSize *= 2
                    }
                }
                
                options.inJustDecodeBounds = false
                options.inSampleSize = inSampleSize
                
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                val outputStream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val compressedBytes = outputStream.toByteArray()
                val base64String = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                
                val dataUri = "data:image/jpeg;base64,$base64String"
                val finalComment = comment.copy(imageUrl = dataUri)
                saveCommentToFirestore(finalComment, continuation)
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        } else {
            saveCommentToFirestore(comment, continuation)
        }
    }

    private fun saveCommentToFirestore(comment: Comment, continuation: kotlin.coroutines.Continuation<Result<Unit>>) {
        val ref = db.collection("comments").document()
        val commentWithId = comment.copy(id = ref.id)
        ref.set(commentWithId)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }
}
