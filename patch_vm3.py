with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

toggle_func = """
    fun toggleEpisodeWatched(showId: Int, episodeKey: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TvShowsUiState.Success) {
                val show = currentState.watchlist.find { it.id == showId } ?: return@launch
                val newWatched = show.watchedEpisodes.toMutableList()
                if (newWatched.contains(episodeKey)) {
                    newWatched.remove(episodeKey)
                } else {
                    newWatched.add(episodeKey)
                }
                firestoreRepository.addToWatchlist(show.copy(watchedEpisodes = newWatched))
            }
        }
    }

    private fun fetchData() {"""

if "fun toggleEpisodeWatched" not in content:
    content = content.replace("    private fun fetchData() {", toggle_func)
    with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'w') as f:
        f.write(content)
