import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''    data class Success(val mediaItem: MediaItem, val isInWatchlist: Boolean) : DetailsUiState()''',
'''    data class Success(
        val mediaItem: MediaItem,
        val isInWatchlist: Boolean,
        val firestoreItem: FirestoreMediaItem? = null,
        val selectedSeasonDetails: com.example.data.remote.SeasonDetails? = null,
        val selectedSeasonNumber: Int? = null,
        val isLoadingSeason: Boolean = false
    ) : DetailsUiState()'''
)

content = content.replace(
'''            val result = repository.getMediaDetails(apiKey, mediaId, mediaType)

            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val isInWatchlist = mediaList.any { it.id == mediaId }
                
                result.onSuccess { response ->
                    _uiState.value = DetailsUiState.Success(response, isInWatchlist)
                }.onFailure { exception ->
                    _uiState.value = DetailsUiState.Error(exception.message ?: "Unknown error occurred")
                }
            }''',
'''            val result = repository.getMediaDetails(apiKey, mediaId, mediaType)

            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val firestoreItem = mediaList.find { it.id == mediaId }
                val isInWatchlist = firestoreItem != null
                
                result.onSuccess { response ->
                    val currentState = _uiState.value
                    var seasonDetails = if (currentState is DetailsUiState.Success) currentState.selectedSeasonDetails else null
                    var seasonNumber = if (currentState is DetailsUiState.Success) currentState.selectedSeasonNumber else null
                    
                    if (mediaType == "tv" && seasonDetails == null && !response.seasons.isNullOrEmpty()) {
                        val firstSeason = response.seasons.firstOrNull { it.season_number > 0 } ?: response.seasons.first()
                        seasonNumber = firstSeason.season_number
                        // We will fetch season details separately
                        fetchSeasonDetails(seasonNumber, apiKey)
                    }

                    _uiState.value = DetailsUiState.Success(
                        mediaItem = response,
                        isInWatchlist = isInWatchlist,
                        firestoreItem = firestoreItem,
                        selectedSeasonDetails = seasonDetails,
                        selectedSeasonNumber = seasonNumber
                    )
                }.onFailure { exception ->
                    _uiState.value = DetailsUiState.Error(exception.message ?: "Unknown error occurred")
                }
            }''')

content = content.replace(
'''    fun toggleWatchlist() {''',
'''    private fun fetchSeasonDetails(seasonNumber: Int, apiKey: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailsUiState.Success) {
                _uiState.value = currentState.copy(isLoadingSeason = true)
                val seasonResult = repository.getSeasonDetails(apiKey, mediaId, seasonNumber)
                seasonResult.onSuccess { seasonDetails ->
                    val newState = _uiState.value
                    if (newState is DetailsUiState.Success) {
                        _uiState.value = newState.copy(
                            selectedSeasonDetails = seasonDetails,
                            selectedSeasonNumber = seasonNumber,
                            isLoadingSeason = false
                        )
                    }
                }.onFailure {
                    val newState = _uiState.value
                    if (newState is DetailsUiState.Success) {
                        _uiState.value = newState.copy(isLoadingSeason = false)
                    }
                }
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") return
        fetchSeasonDetails(seasonNumber, apiKey)
    }

    fun toggleEpisode(seasonNumber: Int, episodeNumber: Int) {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val firestoreItem = currentState.firestoreItem
            if (firestoreItem != null) {
                val episodeKey = "S${seasonNumber}E${episodeNumber}"
                val currentWatched = firestoreItem.watchedEpisodes.toMutableList()
                if (currentWatched.contains(episodeKey)) {
                    currentWatched.remove(episodeKey)
                } else {
                    currentWatched.add(episodeKey)
                }
                
                viewModelScope.launch {
                    firestoreRepository.addOrUpdateMedia(
                        firestoreItem.copy(watchedEpisodes = currentWatched)
                    )
                }
            }
        }
    }

    fun toggleWatchlist() {'''
)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)

