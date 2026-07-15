import re

file_path = "app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """    fun markEpisodeWatched(showId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            firestoreRepository.markEpisodeWatched(showId, season, episode)
        }
    }

    fun markEpisodeUnwatched(showId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            firestoreRepository.markEpisodeUnwatched(showId, season, episode)
        }
    }"""

replacement = """    fun markEpisodeWatched(showId: String, season: Int, episode: Int) {
        val currentState = _uiState.value
        if (currentState is TvShowsUiState.Success) {
            val watchedAt = System.currentTimeMillis()
            var nextEp: NextEpisodeData? = currentState.watchNext.find { it.showId == showId && it.seasonNumber == season && it.episodeNumber == episode }
            if (nextEp == null) {
                nextEp = currentState.notWatchedForAWhile.find { it.showId == showId && it.seasonNumber == season && it.episodeNumber == episode }
            }
            
            val newHistory = currentState.watchedHistory.toMutableList()
            val newWatchNext = currentState.watchNext.toMutableList()
            val newNotWatched = currentState.notWatchedForAWhile.toMutableList()
            val newNotStarted = currentState.notStarted.toMutableList()
            var added = false
            
            if (nextEp != null) {
                newWatchNext.remove(nextEp)
                newNotWatched.remove(nextEp)
                
                newHistory.add(WatchedEpisodeData(
                    showId = nextEp.showId,
                    showName = nextEp.showName,
                    backdropPath = nextEp.backdropPath,
                    posterPath = nextEp.posterPath,
                    seasonNumber = nextEp.seasonNumber,
                    episodeNumber = nextEp.episodeNumber,
                    episodeName = nextEp.episodeName,
                    watchedAt = watchedAt
                ))
                added = true
            } else {
                val notStartedShow = currentState.notStarted.find { it.showId == showId }
                if (notStartedShow != null) {
                    newNotStarted.remove(notStartedShow)
                    newHistory.add(WatchedEpisodeData(
                        showId = notStartedShow.showId,
                        showName = notStartedShow.showName,
                        backdropPath = null,
                        posterPath = notStartedShow.posterPath,
                        seasonNumber = season,
                        episodeNumber = episode,
                        episodeName = "Episode $episode",
                        watchedAt = watchedAt
                    ))
                    added = true
                }
            }
            
            if (added) {
                // Sort history ascending
                newHistory.sortWith(compareBy<WatchedEpisodeData> { it.watchedAt }
                    .thenBy { it.seasonNumber }
                    .thenBy { it.episodeNumber })
                    
                _uiState.value = currentState.copy(
                    watchedHistory = newHistory,
                    watchNext = newWatchNext,
                    notWatchedForAWhile = newNotWatched,
                    notStarted = newNotStarted
                )
            }
        }

        viewModelScope.launch {
            try {
                firestoreRepository.markEpisodeWatched(showId, season, episode)
            } catch (e: Exception) {
                _uiState.value = currentState // Rollback on error
            }
        }
    }

    fun markEpisodeUnwatched(showId: String, season: Int, episode: Int) {
        val currentState = _uiState.value
        if (currentState is TvShowsUiState.Success) {
            val ep = currentState.watchedHistory.find { it.showId == showId && it.seasonNumber == season && it.episodeNumber == episode }
            if (ep != null) {
                val newHistory = currentState.watchedHistory.toMutableList()
                newHistory.remove(ep)
                
                _uiState.value = currentState.copy(
                    watchedHistory = newHistory
                )
            }
        }

        viewModelScope.launch {
            try {
                firestoreRepository.markEpisodeUnwatched(showId, season, episode)
            } catch (e: Exception) {
                _uiState.value = currentState // Rollback on error
            }
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
