package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.FirestoreRepository
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(
        val mediaItem: MediaItem,
        val isInWatchlist: Boolean,
        val firestoreItem: FirestoreMediaItem? = null,
        val selectedSeasonDetails: com.example.data.remote.SeasonDetails? = null,
        val selectedSeasonNumber: Int? = null,
        val isLoadingSeason: Boolean = false,
        val selectedEpisodeDetails: com.example.data.remote.Episode? = null
    ) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

class DetailsViewModel(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository,
    private val mediaId: Int,
    private val mediaType: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState

    init {
        fetchDetails()
    }

    private fun fetchDetails() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            val apiKey = BuildConfig.TMDB_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
                _uiState.value = DetailsUiState.Error("Missing TMDB API Key. Please add it to Secrets.")
                return@launch
            }

            // Fetch Details
            val result = repository.getMediaDetails(apiKey, mediaId, mediaType)

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
            }
        }
    }

    private fun fetchSeasonDetails(seasonNumber: Int, apiKey: String) {
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

    fun selectEpisode(episode: com.example.data.remote.Episode?) {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            _uiState.value = currentState.copy(selectedEpisodeDetails = episode)
        }
    }

    fun toggleEpisode(seasonNumber: Int, episodeNumber: Int) {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val firestoreItem = currentState.firestoreItem
            val episodeKey = "S${seasonNumber}E${episodeNumber}"
            
            viewModelScope.launch {
                if (firestoreItem != null) {
                    val currentWatched = firestoreItem.watchedEpisodes.toMutableList()
                    if (currentWatched.contains(episodeKey)) {
                        currentWatched.remove(episodeKey)
                    } else {
                        currentWatched.add(episodeKey)
                    }
                    firestoreRepository.addOrUpdateMedia(
                        firestoreItem.copy(watchedEpisodes = currentWatched)
                    )
                } else {
                    val item = currentState.mediaItem
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
                }
            }
        }
    }

    fun toggleMovieWatched() {
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
    }

    fun toggleWatchlist() {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val item = currentState.mediaItem
            viewModelScope.launch {
                if (currentState.isInWatchlist) {
                    firestoreRepository.removeMedia(item.id)
                } else {
                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = false,
                            addedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}

class DetailsViewModelFactory(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository,
    private val mediaId: Int,
    private val mediaType: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailsViewModel(repository, firestoreRepository, mediaId, mediaType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
