package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.LocalMediaItem
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val mediaItem: MediaItem, val isInWatchlist: Boolean) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

class DetailsViewModel(
    private val repository: MediaRepository,
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
            
            // Check if in watchlist (we can do a simple collect, or fetch one)
            // For simplicity, we just fetch all and check if ID exists, or we could add a query to Dao
            
            result.onSuccess { response ->
                repository.getLocalMediaByType(mediaType).collect { localList ->
                    val isInWatchlist = localList.any { it.id == mediaId }
                    _uiState.value = DetailsUiState.Success(response, isInWatchlist)
                }
            }.onFailure { exception ->
                _uiState.value = DetailsUiState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun toggleWatchlist() {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            val item = currentState.mediaItem
            viewModelScope.launch {
                if (currentState.isInWatchlist) {
                    repository.deleteLocalMedia(item.id)
                } else {
                    repository.insertLocalMedia(
                        LocalMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType
                        )
                    )
                }
            }
        }
    }
}

class DetailsViewModelFactory(
    private val repository: MediaRepository,
    private val mediaId: Int,
    private val mediaType: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailsViewModel(repository, mediaId, mediaType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
