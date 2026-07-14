import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

replacement = """    suspend fun markEpisodeWatched(showId: String, seasonNumber: Int, episodeNumber: Int) {
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
    }"""

content = re.sub(r'    suspend fun markEpisodeWatched.*?\n    }', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
