package com.example.ui.screens.tvshows

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

sealed class TvShowsUiState {
    object Loading : TvShowsUiState()
    data class Success(val trendingShows: List<MediaItem>, val watchlist: List<FirestoreMediaItem> = emptyList()) : TvShowsUiState()
    data class Error(val message: String) : TvShowsUiState()
}

class TvShowsViewModel(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TvShowsUiState>(TvShowsUiState.Loading)
    val uiState: StateFlow<TvShowsUiState> = _uiState

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.value = TvShowsUiState.Loading
            val apiKey = BuildConfig.TMDB_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
                _uiState.value = TvShowsUiState.Error("Missing TMDB API Key. Please add it to Secrets.")
                return@launch
            }

            // First get TMDB items
            val result = repository.getTrendingTvShows(apiKey)

            // Then observe Firestore
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val tvShows = mediaList.filter { it.mediaType == "tv" }

                result.onSuccess { response ->
                    _uiState.value = TvShowsUiState.Success(
                        trendingShows = response.results,
                        watchlist = tvShows
                    )
                }.onFailure { exception ->
                    _uiState.value = TvShowsUiState.Error(exception.message ?: "Unknown error occurred")
                }
            }
        }
    }
}

class TvShowsViewModelFactory(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TvShowsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TvShowsViewModel(repository, firestoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
