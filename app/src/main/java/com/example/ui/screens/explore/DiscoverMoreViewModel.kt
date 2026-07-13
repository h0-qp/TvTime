package com.example.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverMoreUiState(
    val tvShows: List<MediaItem> = emptyList(),
    val movies: List<MediaItem> = emptyList(),
    val isLoadingMoreTv: Boolean = false,
    val isLoadingMoreMovies: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiscoverMoreViewModel(private val repository: MediaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverMoreUiState(isLoading = true))
    val uiState: StateFlow<DiscoverMoreUiState> = _uiState.asStateFlow()

    private var currentTvPage = 1
    private var isTvLastPage = false
    
    private var currentMoviesPage = 1
    private var isMoviesLastPage = false
    
    private val apiKey = BuildConfig.TMDB_API_KEY

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
            _uiState.update { it.copy(isLoading = false, error = "Missing TMDB API Key") }
            return
        }

        viewModelScope.launch {
            try {
                val tvResult = repository.discoverTv(apiKey = apiKey, page = currentTvPage)
                val moviesResult = repository.discoverMovies(apiKey = apiKey, page = currentMoviesPage)
                
                val tvResponse = tvResult.getOrNull()
                val moviesResponse = moviesResult.getOrNull()
                
                val tvList = tvResponse?.results?.map { it.copy(media_type = "tv") } ?: emptyList()
                val moviesList = moviesResponse?.results?.map { it.copy(media_type = "movie") } ?: emptyList()
                
                _uiState.update { 
                    it.copy(
                        tvShows = tvList,
                        movies = moviesList,
                        isLoading = false
                    )
                }
                
                if (tvResponse != null && tvResponse.page >= tvResponse.total_pages) isTvLastPage = true
                if (moviesResponse != null && moviesResponse.page >= moviesResponse.total_pages) isMoviesLastPage = true
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadMoreTv() {
        if (isTvLastPage || _uiState.value.isLoadingMoreTv || apiKey.isEmpty()) return
        
        currentTvPage++
        _uiState.update { it.copy(isLoadingMoreTv = true) }
        
        viewModelScope.launch {
            try {
                val tvResult = repository.discoverTv(apiKey = apiKey, page = currentTvPage)
                val tvResponse = tvResult.getOrNull()
                
                if (tvResponse != null) {
                    if (tvResponse.page >= tvResponse.total_pages) isTvLastPage = true
                    val newList = tvResponse.results.map { it.copy(media_type = "tv") }
                    
                    _uiState.update { state ->
                        state.copy(
                            tvShows = state.tvShows + newList,
                            isLoadingMoreTv = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingMoreTv = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMoreTv = false) }
            }
        }
    }

    fun loadMoreMovies() {
        if (isMoviesLastPage || _uiState.value.isLoadingMoreMovies || apiKey.isEmpty()) return
        
        currentMoviesPage++
        _uiState.update { it.copy(isLoadingMoreMovies = true) }
        
        viewModelScope.launch {
            try {
                val moviesResult = repository.discoverMovies(apiKey = apiKey, page = currentMoviesPage)
                val moviesResponse = moviesResult.getOrNull()
                
                if (moviesResponse != null) {
                    if (moviesResponse.page >= moviesResponse.total_pages) isMoviesLastPage = true
                    val newList = moviesResponse.results.map { it.copy(media_type = "movie") }
                    
                    _uiState.update { state ->
                        state.copy(
                            movies = state.movies + newList,
                            isLoadingMoreMovies = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingMoreMovies = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMoreMovies = false) }
            }
        }
    }
}

class DiscoverMoreViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoverMoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoverMoreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
