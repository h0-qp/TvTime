import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

# Remove UserProfile from FirestoreRepository.kt
content = re.sub(r'data class UserProfile\([\s\S]*?\)[\n\r]*', '', content)

# Fix WatchlistShow
old_watchlist = """data class WatchlistShow(
    val showId: String = "",
    val showTitle: String = "",
    val posterPath: String? = null,
    val addedAt: Long = 0L
)"""
new_watchlist = """data class WatchlistShow(
    val showId: String = "",
    val addedAt: Long = 0L
)"""
content = content.replace(old_watchlist, new_watchlist)

# Fix observeWatchedEpisodes
old_observe = """    fun observeWatchedEpisodes(showId: String): Flow<List<WatchedEpisode>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("watched_episodes")
            .whereEqualTo("showId", showId)
            .addSnapshotListener { snapshot, error ->"""
new_observe = """    fun observeWatchedEpisodes(): Flow<List<WatchedEpisode>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("watched_episodes")
            .addSnapshotListener { snapshot, error ->"""
content = content.replace(old_observe, new_observe)

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
