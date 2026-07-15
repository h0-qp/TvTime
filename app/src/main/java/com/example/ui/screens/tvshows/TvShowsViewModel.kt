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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Upcoming
data class UpcomingEpisodeData(
    val show: FirestoreMediaItem,
    val showDetails: MediaItem,
    val episodeToAir: EpisodeToAir,
    val daysDifference: Long
)

// Watchlist 
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

data class NotStartedShowData(
    val showId: String,
    val showName: String,
    val posterPath: String?,
    val totalEpisodes: Int
)

sealed class TvShowsUiState {
    object Loading : TvShowsUiState()
    data class Success(
        val upcomingEpisodes: List<UpcomingEpisodeData>,
        val watchedHistory: List<WatchedEpisodeData>,
        val watchNext: List<NextEpisodeData>,
        val notWatchedForAWhile: List<NextEpisodeData>,
        val notStarted: List<NotStartedShowData>
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

    fun markEpisodeWatched(showId: String, season: Int, episode: Int) {
        val currentState = _uiState.value
        if (currentState is TvShowsUiState.Success) {
            val watchedAt = System.currentTimeMillis()
            var nextEp: NextEpisodeData? = currentState.watchNext.find { it.showId == showId && it.seasonNumber == season && it.episodeNumber == episode }
            if (nextEp == null) {
                nextEp = currentState.notWatchedForAWhile.find { it.showId == showId && it.seasonNumber == season && it.episodeNumber == episode }
            }
            
            val newHistory = currentState.watchedHistory.toMutableList()
            val newWatchNext = currentState.watchNext.toMutableList()
            val newNotWatched = currentState.notWatchedForAWhile.toMutableList()
            val newNotStarted = currentState.notStarted.toMutableList()
            var added = false
            
            if (nextEp != null) {
                newWatchNext.remove(nextEp)
                newNotWatched.remove(nextEp)
                
                newHistory.add(WatchedEpisodeData(
                    showId = nextEp.showId,
                    showName = nextEp.showName,
                    backdropPath = nextEp.backdropPath,
                    posterPath = nextEp.posterPath,
                    seasonNumber = nextEp.seasonNumber,
                    episodeNumber = nextEp.episodeNumber,
                    episodeName = nextEp.episodeName,
                    watchedAt = watchedAt
                ))
                added = true
            } else {
                val notStartedShow = currentState.notStarted.find { it.showId == showId }
                if (notStartedShow != null) {
                    newNotStarted.remove(notStartedShow)
                    newHistory.add(WatchedEpisodeData(
                        showId = notStartedShow.showId,
                        showName = notStartedShow.showName,
                        backdropPath = null,
                        posterPath = notStartedShow.posterPath,
                        seasonNumber = season,
                        episodeNumber = episode,
                        episodeName = "Episode $episode",
                        watchedAt = watchedAt
                    ))
                    added = true
                }
            }
            
            if (added) {
                // Sort history ascending
                newHistory.sortWith(compareBy<WatchedEpisodeData> { it.watchedAt }
                    .thenBy { it.seasonNumber }
                    .thenBy { it.episodeNumber })
                    
                _uiState.value = currentState.copy(
                    watchedHistory = newHistory,
                    watchNext = newWatchNext,
                    notWatchedForAWhile = newNotWatched,
                    notStarted = newNotStarted
                )
            }
        }

        viewModelScope.launch {
            try {
                firestoreRepository.markEpisodeWatched(showId, season, episode)
            } catch (e: Exception) {
                _uiState.value = currentState // Rollback on error
            }
        }
    }

    fun markEpisodeUnwatched(showId: String, season: Int, episode: Int) {
        val currentState = _uiState.value
        if (currentState is TvShowsUiState.Success) {
            val ep = currentState.watchedHistory.find { it.showId == showId && it.seasonNumber == season && it.episodeNumber == episode }
            if (ep != null) {
                val newHistory = currentState.watchedHistory.toMutableList()
                newHistory.remove(ep)
                
                _uiState.value = currentState.copy(
                    watchedHistory = newHistory
                )
            }
        }

        viewModelScope.launch {
            try {
                firestoreRepository.markEpisodeUnwatched(showId, season, episode)
            } catch (e: Exception) {
                _uiState.value = currentState // Rollback on error
            }
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.value = TvShowsUiState.Loading
            val apiKey = BuildConfig.TMDB_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_TMDB_API_KEY") {
                _uiState.value = TvShowsUiState.Error("Missing TMDB API Key. Please add it to Secrets.")
                return@launch
            }

            val today = LocalDate.now()

            combine(
                firestoreRepository.observeWatchlistShows(),
                firestoreRepository.observeWatchedEpisodes(),
                firestoreRepository.observeUserMedia()
            ) { watchlist, watchedEps, allMedia ->
                Triple(watchlist, watchedEps, allMedia)
            }.collectLatest { (watchlist, watchedEps, allMedia) ->
                val showDetailsMap = mutableMapOf<String, MediaItem>()
                val seasonDetailsMap = mutableMapOf<String, List<Episode>>() 

                val tvMedia = allMedia.filter { it.mediaType == "tv" }
                val activeWatchlist = tvMedia.map { WatchlistShow(it.id.toString(), it.addedAt) }

                // 1. Fetch details for watchlist (Watch Next, History)
                val deferredDetails = activeWatchlist.map { show ->
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

                for (show in activeWatchlist) {
                    val details = showDetailsMap[show.showId] ?: continue
                    val showWatchedEps = watchedEps.filter { it.showId == show.showId }.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
                    
                    val showMediaItem = tvMedia.find { it.id.toString() == show.showId }
                    val allWatchedEpKeys = showMediaItem?.watchedEpisodes ?: emptyList()
                    val totalEpisodes = details.number_of_episodes ?: 0

                    if (allWatchedEpKeys.isEmpty()) {
                        notStarted.add(NotStartedShowData(
                            showId = show.showId,
                            showName = details.name ?: "Unknown",
                            posterPath = details.poster_path,
                            totalEpisodes = totalEpisodes
                        ))
                        continue
                    }

                    // Add history
                    for (ep in showWatchedEps) {
                        watchedHistory.add(WatchedEpisodeData(
                            showId = show.showId,
                            showName = details.name ?: "Unknown",
                            backdropPath = details.backdrop_path,
                            posterPath = details.poster_path,
                            seasonNumber = ep.seasonNumber,
                            episodeNumber = ep.episodeNumber,
                            episodeName = "Episode ${ep.episodeNumber}", // Real name fetching can be complex
                            watchedAt = ep.watchedAt
                        ))
                    }

                    if (allWatchedEpKeys.size >= totalEpisodes && totalEpisodes > 0) {
                        continue // Completed
                    }

                    // Find Watch Next
                    var nextEpisodeFound: Episode? = null
                    var foundInSeason: Int = -1

                    val candidateSeasons = details.seasons?.filter { it.season_number > 0 }?.sortedBy { it.season_number } ?: emptyList()

                    if (candidateSeasons.isNotEmpty()) {
                        for (season in candidateSeasons) {
                            val watchedInSeasonCount = allWatchedEpKeys.count { it.startsWith("S${season.season_number}E") }
                            if (watchedInSeasonCount >= season.episode_count) {
                                continue
                            }

                            val cacheKey = "${show.showId}_${season.season_number}"
                            if (!seasonDetailsMap.containsKey(cacheKey)) {
                                val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, season.season_number)
                                val seasonInfo = seasonResult.getOrNull()
                                if (seasonInfo != null) {
                                    seasonDetailsMap[cacheKey] = seasonInfo.episodes
                                }
                            }

                            val seasonEpisodes = seasonDetailsMap[cacheKey] ?: emptyList()
                            val candidate = seasonEpisodes.filter { ep ->
                                val epKey = "S${season.season_number}E${ep.episode_number}"
                                val isEpWatched = allWatchedEpKeys.contains(epKey)
                                if (isEpWatched) return@filter false

                                val epAirDate = try { ep.air_date?.let { LocalDate.parse(it) } } catch (e: Exception) { null }
                                val hasAired = epAirDate == null || !epAirDate.isAfter(today)
                                hasAired
                            }.minByOrNull { it.episode_number }

                            if (candidate != null) {
                                nextEpisodeFound = candidate
                                foundInSeason = season.season_number
                                break
                            }
                        }
                    }

                    if (nextEpisodeFound != null) {
                        val lastWatchedEpFromRecent = showWatchedEps.lastOrNull()
                        val isRecent = if (lastWatchedEpFromRecent != null) {
                            (System.currentTimeMillis() - lastWatchedEpFromRecent.watchedAt) < 30L * 24 * 60 * 60 * 1000
                        } else {
                            false
                        }
                        val nextData = NextEpisodeData(
                            showId = show.showId,
                            showName = details.name ?: "Unknown",
                            backdropPath = details.backdrop_path,
                            posterPath = details.poster_path,
                            seasonNumber = foundInSeason,
                            episodeNumber = nextEpisodeFound.episode_number,
                            episodeName = nextEpisodeFound.name ?: "Episode ${nextEpisodeFound.episode_number}"
                        )
                        if (isRecent) watchNext.add(nextData) else notWatchedForAWhile.add(nextData)
                    }
                }

                // 2. Fetch upcoming episodes for "المرتقبة" tab
                val deferredUpcoming = tvMedia.map { media ->
                    async {
                        var epData: UpcomingEpisodeData? = null
                        val details = showDetailsMap[media.id.toString()] ?: repository.getMediaDetails(apiKey, media.id, "tv").getOrNull()
                        if (details != null) {
                            val nextEpisode = details.next_episode_to_air
                            val lastEpisode = details.last_episode_to_air
                                                        
                            if (nextEpisode != null) {
                                val airDate = try { LocalDate.parse(nextEpisode.air_date) } catch (e: Exception) { null }
                                if (airDate != null) {
                                    val diff = ChronoUnit.DAYS.between(today, airDate)
                                    epData = UpcomingEpisodeData(media, details, nextEpisode, diff)
                                }
                            } else if (lastEpisode != null) {
                                val airDate = try { LocalDate.parse(lastEpisode.air_date) } catch (e: Exception) { null }
                                if (airDate != null) {
                                    val diff = ChronoUnit.DAYS.between(today, airDate)
                                    if (diff >= -30) {
                                        epData = UpcomingEpisodeData(media, details, lastEpisode, diff)
                                    }
                                }
                            }
                        }
                        epData
                    }
                }
                
                val upcomingEpisodes = deferredUpcoming.awaitAll().filterNotNull().sortedBy { it.daysDifference }

                // Sort history ascending
                watchedHistory.sortWith(compareBy<WatchedEpisodeData> { it.watchedAt }
                    .thenBy { it.seasonNumber }
                    .thenBy { it.episodeNumber })

                _uiState.value = TvShowsUiState.Success(
                    upcomingEpisodes = upcomingEpisodes,
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
