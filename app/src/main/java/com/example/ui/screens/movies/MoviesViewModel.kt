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
    data class Success(val trendingMovies: List<MediaItem>, val watchlist: List<FirestoreMediaItem> = emptyList()) : MoviesUiState()
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
            
            // First get TMDB items
            val result = repository.getTrendingMovies(apiKey)
            
            // Then observe Firestore
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val movies = mediaList.filter { it.mediaType == "movie" }
                
                result.onSuccess { response ->
                    _uiState.value = MoviesUiState.Success(
                        trendingMovies = response.results,
                        watchlist = movies
                    )
                }.onFailure { exception ->
                    _uiState.value = MoviesUiState.Error(exception.message ?: "Unknown error occurred")
                }
            }
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
