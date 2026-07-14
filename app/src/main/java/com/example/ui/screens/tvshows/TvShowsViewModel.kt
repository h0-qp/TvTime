package com.example.ui.screens.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.WatchedEpisode
import com.example.data.firebase.WatchlistShow
import com.example.data.remote.MediaItem
import com.example.data.remote.EpisodeToAir
import com.example.data.remote.Episode
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

// Represents the unwatched episode to watch next
data class NextEpisodeData(
    val showId: String,
    val showName: String,
    val backdropPath: String?,
    val posterPath: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeName: String,
    val isWatched: Boolean = false
)

// Represents an episode that was watched
data class WatchedEpisodeData(
    val showId: String,
    val showName: String,
    val backdropPath: String?,
    val posterPath: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeName: String,
    val watchedAt: Long
)

// Represents a show that hasn't been started
data class NotStartedShowData(
    val showId: String,
    val showName: String,
    val posterPath: String?,
    val totalEpisodes: Int
)

sealed class WatchlistUiState {
    object Loading : WatchlistUiState()
    data class Success(
        val watchedHistory: List<WatchedEpisodeData>,
        val watchNext: List<NextEpisodeData>,
        val notWatchedForAWhile: List<NextEpisodeData>,
        val notStarted: List<NotStartedShowData>
    ) : WatchlistUiState()
    data class Error(val message: String) : WatchlistUiState()
}

class TvShowsViewModel(
    private val repository: MediaRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState

    init {
        fetchWatchlistData()
    }

    fun markEpisodeWatched(showId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            firestoreRepository.markEpisodeWatched(showId, season, episode)
        }
    }

    fun markEpisodeUnwatched(showId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            firestoreRepository.markEpisodeUnwatched(showId, season, episode)
        }
    }

    private fun fetchWatchlistData() {
        viewModelScope.launch {
            _uiState.value = WatchlistUiState.Loading
            val apiKey = BuildConfig.TMDB_API_KEY

            if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
                _uiState.value = WatchlistUiState.Error("Missing TMDB API Key. Please add it to Secrets.")
                return@launch
            }

            combine(
                firestoreRepository.observeWatchlistShows(),
                firestoreRepository.observeWatchedEpisodes()
            ) { watchlist, watchedEps ->
                Pair(watchlist, watchedEps)
            }.collectLatest { (watchlist, watchedEps) ->
                val showDetailsMap = mutableMapOf<String, MediaItem>()
                val seasonDetailsMap = mutableMapOf<String, List<Episode>>() // key: showId_seasonNumber

                // Fetch details for all shows in watchlist in parallel
                val deferredDetails = watchlist.map { show ->
                    async {
                        val result = repository.getMediaDetails(apiKey, show.showId.toIntOrNull() ?: 0, "tv")
                        result.getOrNull()?.let { details ->
                            showDetailsMap[show.showId] = details
                        }
                    }
                }
                deferredDetails.awaitAll()

                val watchedHistory = mutableListOf<WatchedEpisodeData>()
                val watchNext = mutableListOf<NextEpisodeData>()
                val notWatchedForAWhile = mutableListOf<NextEpisodeData>()
                val notStarted = mutableListOf<NotStartedShowData>()
                
                val currentTime = System.currentTimeMillis()
                val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000

                // Now determine the next episode for each show
                // To find the next episode, we need to know all episodes for the show.
                // Instead of fetching ALL seasons for ALL shows, let's just fetch the seasons that are relevant.
                // We'll figure out the latest watched episode.
                
                for (show in watchlist) {
                    val details = showDetailsMap[show.showId] ?: continue
                    val showName = details.name ?: details.title ?: "Unknown"
                    
                    val showWatchedEps = watchedEps.filter { it.showId == show.showId }.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
                    
                    if (showWatchedEps.isEmpty()) {
                        notStarted.add(
                            NotStartedShowData(
                                showId = show.showId,
                                showName = showName,
                                posterPath = details.poster_path,
                                totalEpisodes = details.number_of_episodes ?: 0
                            )
                        )
                        continue
                    }
                    
                    // Add watched episodes to history
                    for (ep in showWatchedEps) {
                        watchedHistory.add(
                            WatchedEpisodeData(
                                showId = show.showId,
                                showName = showName,
                                backdropPath = details.backdrop_path,
                                posterPath = details.poster_path,
                                seasonNumber = ep.seasonNumber,
                                episodeNumber = ep.episodeNumber,
                                episodeName = "الحلقة ${ep.episodeNumber}", // Fallback name
                                watchedAt = ep.watchedAt
                            )
                        )
                    }

                    val lastWatched = showWatchedEps.last()
                    val lastWatchedAt = showWatchedEps.maxOf { it.watchedAt }
                    
                    // Need to find the next unwatched episode.
                    // This could be episode + 1 in the same season, or episode 1 in the next season.
                    // We need season details. Let's fetch the last watched season to see if there are more episodes.
                    val seasonRes = repository.getSeasonDetails(apiKey, show.showId.toInt(), lastWatched.seasonNumber)
                    var nextEp: Episode? = null
                    var nextSeasonNum = lastWatched.seasonNumber
                    
                    seasonRes.onSuccess { seasonData ->
                        val nextEpInSameSeason = seasonData.episodes.find { it.episode_number > lastWatched.episodeNumber }
                        if (nextEpInSameSeason != null) {
                            nextEp = nextEpInSameSeason
                        } else {
                            // Check next season
                            val nextSeasonResult = repository.getSeasonDetails(apiKey, show.showId.toInt(), lastWatched.seasonNumber + 1)
                            nextSeasonResult.onSuccess { nextSeasonData ->
                                nextEp = nextSeasonData.episodes.firstOrNull()
                                nextSeasonNum = lastWatched.seasonNumber + 1
                            }
                        }
                    }

                    if (nextEp != null) {
                        val nextEpData = NextEpisodeData(
                            showId = show.showId,
                            showName = showName,
                            backdropPath = nextEp?.still_path ?: details.backdrop_path,
                            posterPath = details.poster_path,
                            seasonNumber = nextSeasonNum,
                            episodeNumber = nextEp?.episode_number ?: 0,
                            episodeName = nextEp?.name ?: "الحلقة ${nextEp?.episode_number ?: 0}"
                        )
                        
                        if (currentTime - lastWatchedAt < thirtyDaysInMillis) {
                            watchNext.add(nextEpData)
                        } else {
                            notWatchedForAWhile.add(nextEpData)
                        }
                    }
                }

                // Fetch real names for watched history if needed? The user just needs the info.
                // We'll skip fetching individual episode details for watched history to avoid rate limits, fallback name is used.
                
                // Sort history
                watchedHistory.sortByDescending { it.watchedAt }
                
                _uiState.value = WatchlistUiState.Success(
                    watchedHistory = watchedHistory,
                    watchNext = watchNext,
                    notWatchedForAWhile = notWatchedForAWhile,
                    notStarted = notStarted
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
