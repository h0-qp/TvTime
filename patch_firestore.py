import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

new_models = """
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
"""

if 'data class WatchlistShow' not in content:
    content = content.replace('class FirestoreRepository', new_models + '\nclass FirestoreRepository')

# We need to add the observe methods
new_methods = """
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
    }

    suspend fun markEpisodeUnwatched(showId: String, seasonNumber: Int, episodeNumber: Int) {
        val uid = currentUserId ?: return
        val docId = "${showId}_S${seasonNumber}E${episodeNumber}"
        db.collection("users").document(uid).collection("watched_episodes").document(docId).delete().await()
    }
"""

if 'observeWatchlistShows' not in content:
    content = content.replace('fun observeUserMedia(): Flow<List<FirestoreMediaItem>> = callbackFlow {', new_methods + '\n    fun observeUserMedia(): Flow<List<FirestoreMediaItem>> = callbackFlow {')

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
