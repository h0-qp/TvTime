package com.example.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ExploreUiState {
    object Idle : ExploreUiState()
    object Loading : ExploreUiState()
    data class Success(
        val results: List<MediaItem>,
        val trendingTvShows: List<MediaItem> = emptyList(),
        val trendingMovies: List<MediaItem> = emptyList(),
        val upcomingTvShows: List<MediaItem> = emptyList()
    ) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

class ExploreViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var searchJob: Job? = null

    init {
        loadDiscoverData()
    }

    private fun loadDiscoverData() {
        viewModelScope.launch {
            val apiKey = BuildConfig.TMDB_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
                _uiState.value = ExploreUiState.Error("Missing TMDB API Key. Please add it to Secrets.")
                return@launch
            }

            try {
                val trendingTv = repository.getTrendingTvShows(apiKey).getOrNull()?.results ?: emptyList()
                val trendingMovies = repository.getTrendingMovies(apiKey).getOrNull()?.results ?: emptyList()
                val upcomingTv = repository.getUpcomingTvShows(apiKey).getOrNull()?.results ?: emptyList()
                
                _uiState.value = ExploreUiState.Success(
                    results = emptyList(),
                    trendingTvShows = trendingTv,
                    trendingMovies = trendingMovies,
                    upcomingTvShows = upcomingTv
                )
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            loadDiscoverData()
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        val currentState = _uiState.value
        _uiState.value = ExploreUiState.Loading
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
            _uiState.value = ExploreUiState.Error("Missing TMDB API Key.")
            return
        }
        val result = repository.searchMulti(apiKey, query)
        result.onSuccess { response ->
            val filteredResults = response.results.filter { 
                it.media_type == "tv" || it.media_type == "movie" 
            }
            if (currentState is ExploreUiState.Success) {
                _uiState.value = currentState.copy(results = filteredResults)
            } else {
                _uiState.value = ExploreUiState.Success(results = filteredResults)
            }
        }.onFailure { exception ->
            _uiState.value = ExploreUiState.Error(exception.message ?: "Unknown error occurred")
        }
    }
}

class ExploreViewModelFactory(
    private val repository: MediaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExploreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
