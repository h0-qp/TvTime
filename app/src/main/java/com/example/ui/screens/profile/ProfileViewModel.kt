package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreRepository
import com.example.data.repository.MediaRepository
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class ProfileViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _totalEpisodesWatched = MutableStateFlow(0)
    val totalEpisodesWatched: StateFlow<Int> = _totalEpisodesWatched.asStateFlow()

    private val _tvTimeMinutes = MutableStateFlow(0)
    val tvTimeMinutes: StateFlow<Int> = _tvTimeMinutes.asStateFlow()

    private val _tvShowsCount = MutableStateFlow(0)
    val tvShowsCount: StateFlow<Int> = _tvShowsCount.asStateFlow()

    private val _moviesCount = MutableStateFlow(0)
    val moviesCount: StateFlow<Int> = _moviesCount.asStateFlow()

    init {
        viewModelScope.launch {
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val tvMedia = mediaList.filter { it.mediaType == "tv" }
                val movies = mediaList.filter { it.mediaType == "movie" }

                _tvShowsCount.value = tvMedia.size
                _moviesCount.value = movies.size

                _totalEpisodesWatched.value = tvMedia.sumOf { it.watchedEpisodes.size }
                
                // Calculate TV time by fetching details of each show to get episode run time
                // For simplicity/performance, we can average 45 mins, but fetching gives accurate data.
                var totalMins = 0
                val apiKey = BuildConfig.TMDB_API_KEY
                
                val deferredRuntimes = tvMedia.map { item ->
                    async {
                        val result = mediaRepository.getMediaDetails(apiKey, item.id, "tv")
                        val details = result.getOrNull()
                        val episodesCount = item.watchedEpisodes.size
                        
                        var runtime = 45 // fallback
                        if (details?.episode_run_time != null && details.episode_run_time.isNotEmpty()) {
                            runtime = details.episode_run_time.first()
                        }
                        
                        episodesCount * runtime
                    }
                }
                
                totalMins = deferredRuntimes.awaitAll().sum()
                _tvTimeMinutes.value = totalMins
            }
        }
    }
}

class ProfileViewModelFactory(
    private val firestoreRepository: FirestoreRepository,
    private val mediaRepository: MediaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(firestoreRepository, mediaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
