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
    val daysDifference: Long,
    val showNewBadge: Boolean = false
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

    private val detailsCache = mutableMapOf<String, com.example.data.remote.MediaItem>()
    private val seasonCache = mutableMapOf<String, List<com.example.data.remote.Episode>>()

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
                val lastWatchedMap = mutableMapOf<String, com.example.data.firebase.WatchedEpisode>() 

                val tvMedia = allMedia.filter { it.mediaType == "tv" }
                val activeWatchlist = tvMedia.map { WatchlistShow(it.id.toString(), it.addedAt) }

                // 1. Fetch details for watchlist (Watch Next, History)
                val deferredDetails = activeWatchlist.filter { !detailsCache.containsKey(it.showId) }.map { show ->
                    async {
                        val result = repository.getMediaDetails(apiKey, show.showId.toIntOrNull() ?: 0, "tv")
                        Pair(show.showId, result.getOrNull())
                    }
                }
                val results = deferredDetails.awaitAll()
                for ((showId, details) in results) {
                    if (details != null) {
                        detailsCache[showId] = details
                    }
                }

                for (show in activeWatchlist) {
                    val lastWatchedEp = watchedEps.filter { it.showId == show.showId }.maxByOrNull { it.watchedAt }
                    if (lastWatchedEp != null) {
                        lastWatchedMap[show.showId] = lastWatchedEp
                    }
                }

                // 1.5 Fetch necessary season details concurrently
                val deferredSeasons = activeWatchlist.mapNotNull { show ->
                    val details = detailsCache[show.showId] ?: return@mapNotNull null
                    val showMediaItem = tvMedia.find { it.id.toString() == show.showId }
                    val allWatchedEpKeys = showMediaItem?.watchedEpisodes ?: emptyList()
                    val totalEpisodes = details.number_of_episodes ?: 0
                    
                    if (allWatchedEpKeys.isEmpty() || (allWatchedEpKeys.size >= totalEpisodes && totalEpisodes > 0)) {
                        return@mapNotNull null // Not started or completed
                    }
                    
                    val candidateSeasons = details.seasons?.filter { it.season_number > 0 }?.sortedBy { it.season_number } ?: emptyList()
                    val firstUnwatchedSeason = candidateSeasons.firstOrNull { season ->
                        val watchedInSeasonCount = allWatchedEpKeys.count { it.startsWith("S${season.season_number}E") }
                        watchedInSeasonCount < season.episode_count
                    }
                    
                    if (firstUnwatchedSeason != null) {
                        val cacheKey = "${show.showId}_${firstUnwatchedSeason.season_number}"
                        if (seasonCache.containsKey(cacheKey)) {
                            null
                        } else {
                            async {
                                val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, firstUnwatchedSeason.season_number)
                                val seasonInfo = seasonResult.getOrNull()
                                Pair(cacheKey, seasonInfo?.episodes)
                            }
                        }
                    } else {
                        null
                    }
                }
                
                val seasonResults = deferredSeasons.awaitAll()
                for ((cacheKey, episodes) in seasonResults) {
                    if (episodes != null) {
                        seasonCache[cacheKey] = episodes
                    }
                }

                val watchedHistory = mutableListOf<WatchedEpisodeData>()
                val watchNext = mutableListOf<NextEpisodeData>()
                val notWatchedForAWhile = mutableListOf<NextEpisodeData>()
                val notStarted = mutableListOf<NotStartedShowData>()

                for (show in activeWatchlist) {
                    val details = detailsCache[show.showId] ?: continue
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
                            if (!seasonCache.containsKey(cacheKey)) {
                                val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, season.season_number)
                                val seasonInfo = seasonResult.getOrNull()
                                if (seasonInfo != null) {
                                    seasonCache[cacheKey] = seasonInfo.episodes
                                }
                            }

                            val seasonEpisodes = seasonCache[cacheKey] ?: emptyList()
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
                        val lastWatchedEp = lastWatchedMap[show.showId]
                        val isRecent = if (lastWatchedEp != null) {
                            (System.currentTimeMillis() - lastWatchedEp.watchedAt) <= 30L * 24 * 60 * 60 * 1000
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
                        val epDataList = mutableListOf<UpcomingEpisodeData>()
                        var details = detailsCache[media.id.toString()]
                        if (details == null) {
                            details = repository.getMediaDetails(apiKey, media.id, "tv").getOrNull()
                            if (details != null) {
                                detailsCache[media.id.toString()] = details
                            }
                        }
                        if (details != null) {
                            val nextEpisode = details.next_episode_to_air
                            val lastEpisode = details.last_episode_to_air
                                                        
                            val targetSeason = nextEpisode?.season_number ?: lastEpisode?.season_number
                            
                            if (targetSeason != null) {
                                val seasonDetails = repository.getSeasonDetails(apiKey, media.id, targetSeason).getOrNull()
                                
                                val processEpisode = { ep: EpisodeToAir? ->
                                    if (ep != null && ep.air_date?.isNotEmpty() == true) {
                                        val airDate = try { LocalDate.parse(ep.air_date) } catch (e: Exception) { null }
                                        if (airDate != null) {
                                            val diff = ChronoUnit.DAYS.between(today, airDate)
                                            if (diff >= -30) {
                                                val showNewBadge = (diff == 0L || diff == -1L)
                                                epDataList.add(UpcomingEpisodeData(media, details, ep, diff, showNewBadge))
                                            }
                                        }
                                    }
                                }
                                
                                if (seasonDetails != null) {
                                    seasonDetails.episodes.forEach { episode ->
                                        if (episode.air_date != null && episode.air_date.isNotEmpty()) {
                                            val airDate = try { LocalDate.parse(episode.air_date) } catch (e: Exception) { null }
                                            if (airDate != null) {
                                                val diff = ChronoUnit.DAYS.between(today, airDate)
                                                if (diff >= -30) {
                                                    val showNewBadge = (diff == 0L || diff == -1L)
                                                    val episodeToAir = EpisodeToAir(
                                                        id = episode.id,
                                                        name = episode.name,
                                                        overview = episode.overview,
                                                        air_date = episode.air_date ?: "",
                                                        episode_number = episode.episode_number,
                                                        season_number = episode.season_number,
                                                        still_path = episode.still_path
                                                    )
                                                    epDataList.add(UpcomingEpisodeData(media, details, episodeToAir, diff, showNewBadge))
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Ensure lastEpisode is included if it wasn't in seasonDetails (e.g. from previous season)
                                if (lastEpisode != null && epDataList.none { it.episodeToAir.id == lastEpisode.id }) {
                                    processEpisode(lastEpisode)
                                }
                                // Ensure nextEpisode is included if seasonDetails failed or missed it
                                if (nextEpisode != null && epDataList.none { it.episodeToAir.id == nextEpisode.id }) {
                                    processEpisode(nextEpisode)
                                }
                            }
                        }
                        epDataList
                    }
                }
                
                val upcomingEpisodes = deferredUpcoming.awaitAll().flatten().sortedBy { it.daysDifference }

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
