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
    val isWatched: Boolean = false,
    val addedAt: Long = 0L,
    val watchedEpisodes: List<String> = emptyList()
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
}
