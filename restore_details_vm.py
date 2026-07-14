import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

# I need to fix toggleMovieWatched and toggleEpisode.
# Let's completely replace the two functions.

func_toggle_episode = """    fun toggleEpisode(seasonNumber: Int, episodeNumber: Int) {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val firestoreItem = currentState.firestoreItem
            
            viewModelScope.launch {
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
            }
        }
    }"""

func_toggle_movie_watched = """    fun toggleMovieWatched() {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success && mediaType == "movie") {
            val firestoreItem = currentState.firestoreItem
            viewModelScope.launch {
                if (firestoreItem != null) {
                    firestoreRepository.addOrUpdateMedia(
                        firestoreItem.copy(isWatched = !firestoreItem.isWatched)
                    )
                } else {
                    val item = currentState.mediaItem
                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = true,
                            addedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }"""

# Since I messed up the file, I'll find where toggleEpisode starts and replace from there to toggleWatchlist.
start_idx = content.find("fun toggleEpisode(")
end_idx = content.find("fun toggleWatchlist()")

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + func_toggle_episode + "\n\n" + func_toggle_movie_watched + "\n\n    " + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
        f.write(new_content)
        print("Success")
else:
    print("Could not find boundaries")
