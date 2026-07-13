#!/bin/bash
sed -i 's/init {/init {\n        observeWatchlist()\n/g' app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

    private fun observeWatchlist() {
        viewModelScope.launch {
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val ids = mediaList.map { it.id }.toSet()
                val currentState = _uiState.value
                if (currentState is ExploreUiState.Success) {
                    _uiState.value = currentState.copy(watchlistIds = ids)
                }
            }
        }
    }

    fun toggleWatchlist(item: MediaItem) {
        viewModelScope.launch {
            val currentState = _uiState.value as? ExploreUiState.Success ?: return@launch
            val isAdded = currentState.watchlistIds.contains(item.id)
            if (isAdded) {
                firestoreRepository.removeMedia(item.id)
            } else {
                firestoreRepository.addOrUpdateMedia(
                    FirestoreMediaItem(
                        id = item.id,
                        title = item.name ?: item.title ?: "Unknown",
                        posterPath = item.poster_path,
                        mediaType = item.media_type ?: "tv",
                        isWatched = false,
                        addedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
INNER_EOF

# Ensure the closing brace of the class is correct, wait we just appended to the end of the file. So it's outside the class!
# Let's fix this by removing the last `}` and putting it after our new code.

