package com.example.ui.screens.movies

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

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(
        val watchlist: List<FirestoreMediaItem> = emptyList(),
        val movieDetails: Map<Int, MediaItem> = emptyMap()
    ) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

class MoviesViewModel(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            val apiKey = BuildConfig.TMDB_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
                _uiState.value = MoviesUiState.Error("Missing TMDB API Key. Please add it to Secrets.")
                return@launch
            }

            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val watchlistMovies = mediaList.filter { it.mediaType == "movie" && !it.watched }
                
                val currentState = _uiState.value
                val existingDetails = if (currentState is MoviesUiState.Success) currentState.movieDetails else emptyMap()
                
                // Set initial state immediately with watchlist and cached details
                _uiState.value = MoviesUiState.Success(
                    watchlist = watchlistMovies,
                    movieDetails = existingDetails
                )
                
                // Fetch TMDB details for any new watchlist items
                viewModelScope.launch {
                    val updatedDetails = existingDetails.toMutableMap()
                    var hasNewDetails = false
                    
                    watchlistMovies.forEach { movie ->
                        if (!updatedDetails.containsKey(movie.id)) {
                            try {
                                val result = repository.getMediaDetails(apiKey, movie.id, "movie")
                                result.onSuccess { details ->
                                    updatedDetails[movie.id] = details
                                    hasNewDetails = true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    
                    if (hasNewDetails || existingDetails.size != updatedDetails.size) {
                        _uiState.value = MoviesUiState.Success(
                            watchlist = watchlistMovies,
                            movieDetails = updatedDetails
                        )
                    }
                }
            }
        }
    }

    fun markAsWatched(movie: FirestoreMediaItem) {
        viewModelScope.launch {
            firestoreRepository.addOrUpdateMedia(movie.copy(watched = true))
        }
    }
}

class MoviesViewModelFactory(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoviesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoviesViewModel(repository, firestoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
