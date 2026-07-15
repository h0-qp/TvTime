import re

file_path = "app/src/main/java/com/example/data/firebase/FirestoreRepository.kt"

with open(file_path, "r") as f:
    content = f.read()

new_logic = """
        try {
            // Fetch or update media item first
            val mediaRef = db.collection("users").document(uid).collection("media").document(showId)
            val snapshot = mediaRef.get().await()
            val newEpisodeKeys = episodes.map { "S${it.first}E${it.second}" }
            
            var currentMediaList = mutableListOf<String>()
            if (snapshot.exists()) {
                val item = snapshot.toObject(FirestoreMediaItem::class.java)
                if (item != null) {
                    currentMediaList = item.watchedEpisodes.toMutableList()
                    for (key in newEpisodeKeys) {
                        if (!currentMediaList.contains(key)) {
                            currentMediaList.add(key)
                        }
                    }
                }
            }
            
            // Chunk episodes into batches of 400
            val chunkedEpisodes = episodes.chunked(400)
            
            for ((index, chunk) in chunkedEpisodes.withIndex()) {
                val batch = db.batch()
                
                for ((seasonNumber, episodeNumber) in chunk) {
                    val docId = "${showId}_S${seasonNumber}E${episodeNumber}"
                    val docRef = db.collection("users").document(uid).collection("watched_episodes").document(docId)
                    batch.set(docRef, WatchedEpisode(showId, seasonNumber, episodeNumber, System.currentTimeMillis()))
                }
                
                // Update media item in the last batch
                if (index == chunkedEpisodes.size - 1) {
                    if (snapshot.exists()) {
                        batch.update(mediaRef, "watchedEpisodes", currentMediaList)
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
                }
                
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
"""

# Replace the block
pattern = r"        try \{\s*val batch = db\.batch\(\).*?batch\.commit\(\)\.await\(\)\s*\} catch \(e: Exception\) \{"
content = re.sub(pattern, new_logic.strip() + "\n        } catch (e: Exception) {", content, flags=re.DOTALL)

with open(file_path, "w") as f:
    f.write(content)
