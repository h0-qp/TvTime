package com.example.ui.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.FirestoreRepository
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val movies = mediaList.filter { it.mediaType == "movie" }
                
                // Set initial state immediately with watchlist
                val currentState = _uiState.value
                val previousUpcoming = if (currentState is MoviesUiState.Success) currentState.trendingMovies else emptyList()
                
                _uiState.value = MoviesUiState.Success(
                    trendingMovies = previousUpcoming,
                    watchlist = movies
                )
                
                // Fetch English details and find upcoming movies from watchlist
                viewModelScope.launch {
                    val deferreds = movies.map { movie ->
                        async {
                            val detailsResult = repository.getMediaDetails(apiKey, movie.id, "movie")
                            var upcomingItem: MediaItem? = null
                            detailsResult.onSuccess { details ->
                                val englishTitle = details.title ?: details.name
                                if (englishTitle != null && movie.title != englishTitle) {
                                    firestoreRepository.addOrUpdateMedia(
                                        movie.copy(
                                            title = englishTitle,
                                            posterPath = details.poster_path ?: movie.posterPath
                                        )
                                    )
                                }
                                
                                val releaseDateStr = details.release_date
                                var isUpcoming = false
                                if (!releaseDateStr.isNullOrEmpty()) {
                                    try {
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        val releaseDate = dateFormat.parse(releaseDateStr)
                                        if (releaseDate != null && releaseDate.after(Date())) {
                                            isUpcoming = true
                                        }
                                    } catch (e: Exception) {}
                                } else if (details.status != "Released" && details.status != "Canceled" && details.status != null) {
                                    isUpcoming = true
                                }
                                
                                if (isUpcoming) {
                                    upcomingItem = details
                                }
                            }
                            upcomingItem
                        }
                    }
                    
                    val results = deferreds.awaitAll()
                    val upcomingMovies = results.filterNotNull().sortedBy { it.release_date }
                    
                    _uiState.value = MoviesUiState.Success(
                        trendingMovies = upcomingMovies,
                        watchlist = movies
                    )
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
