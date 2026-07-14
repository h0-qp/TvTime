import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

replacement = """            viewModelScope.launch {
                if (firestoreItem != null) {
                    val currentWatched = firestoreItem.watchedEpisodes.toMutableList()
                    val episodeKey = "S${seasonNumber}E${episodeNumber}"
                    if (currentWatched.contains(episodeKey)) {
                        firestoreRepository.markEpisodeUnwatched(currentState.mediaItem.id.toString(), seasonNumber, episodeNumber)
                    } else {
                        firestoreRepository.markEpisodeWatched(currentState.mediaItem.id.toString(), seasonNumber, episodeNumber)
                    }
                } else {
                    val item = currentState.mediaItem
                    val episodeKey = "S${seasonNumber}E${episodeNumber}"
                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = false,
                            watchedEpisodes = listOf(episodeKey),
                            addedAt = System.currentTimeMillis()
                        )
                    )
                    firestoreRepository.markEpisodeWatched(item.id.toString(), seasonNumber, episodeNumber)
                    if (mediaType == "tv") {
                        firestoreRepository.addTvShowToWatchlist(item.id.toString())
                    }
                }
            }"""

content = re.sub(r'            viewModelScope\.launch \{\n                if \(firestoreItem != null\) \{.*?\n                \}\n            \}', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)
