import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

replacement = '''package com.example.ui.screens.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.FirestoreRepository
import com.example.data.remote.MediaItem
import com.example.data.remote.EpisodeToAir
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class UpcomingEpisodeData(
    val show: FirestoreMediaItem,
    val showDetails: MediaItem,
    val episodeToAir: EpisodeToAir,
    val daysDifference: Long
)

sealed class TvShowsUiState {
    object Loading : TvShowsUiState()
    data class Success(
        val trendingShows: List<MediaItem>, 
        val watchlist: List<FirestoreMediaItem> = emptyList(),
        val upcomingEpisodes: List<UpcomingEpisodeData> = emptyList()
    ) : TvShowsUiState()
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
            val result = repository.getUpcomingTvShows(apiKey)
            val trendingShows = result.getOrNull()?.results ?: emptyList()

            // Then observe Firestore
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val tvShows = mediaList.filter { it.mediaType == "tv" }
                
                val today = LocalDate.now()
                
                // Fetch details for each TV show in watchlist
                val deferredEpisodes = tvShows.map { show ->
                    async {
                        val detailsResult = repository.getMediaDetails(apiKey, show.id, "tv")
                        var epData: UpcomingEpisodeData? = null
                        detailsResult.onSuccess { details ->
                            val nextEpisode = details.next_episode_to_air
                            val lastEpisode = details.last_episode_to_air
                            
                            if (nextEpisode != null) {
                                val airDate = try { LocalDate.parse(nextEpisode.air_date) } catch (e: Exception) { null }
                                if (airDate != null) {
                                    val diff = ChronoUnit.DAYS.between(today, airDate)
                                    epData = UpcomingEpisodeData(show, details, nextEpisode, diff)
                                }
                            } else if (lastEpisode != null) {
                                val airDate = try { LocalDate.parse(lastEpisode.air_date) } catch (e: Exception) { null }
                                if (airDate != null) {
                                    val diff = ChronoUnit.DAYS.between(today, airDate)
                                    if (diff >= -30) {
                                        epData = UpcomingEpisodeData(show, details, lastEpisode, diff)
                                    }
                                }
                            }
                        }
                        epData
                    }
                }
                
                val upcomingEpisodes = deferredEpisodes.awaitAll().filterNotNull().sortedBy { it.daysDifference }

                _uiState.value = TvShowsUiState.Success(
                    trendingShows = trendingShows,
                    watchlist = tvShows,
                    upcomingEpisodes = upcomingEpisodes
                )
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
'''

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'w') as f:
    f.write(replacement)

