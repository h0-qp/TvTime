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
        viewModelScope.launch {
            firestoreRepository.markEpisodeWatched(showId, season, episode)
        }
    }

    fun markEpisodeUnwatched(showId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            firestoreRepository.markEpisodeUnwatched(showId, season, episode)
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
                    
                    if (showWatchedEps.isEmpty()) {
                        notStarted.add(NotStartedShowData(
                            showId = show.showId,
                            showName = details.name ?: "Unknown",
                            posterPath = details.poster_path,
                            totalEpisodes = details.number_of_episodes ?: 0
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

                    // Find Watch Next
                    val lastWatched = showWatchedEps.last()
                    // Fetch season details if not cached
                    if (!seasonDetailsMap.containsKey("${show.showId}_${lastWatched.seasonNumber}")) {
                        val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, lastWatched.seasonNumber)
                        val seasonInfo = seasonResult.getOrNull()
                        if (seasonInfo != null) {
                            seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber}"] = seasonInfo.episodes
                        }
                    }

                    val currentSeasonEpisodes = seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber}"] ?: emptyList()
                    val nextEpInSameSeason = currentSeasonEpisodes.find { it.episode_number == lastWatched.episodeNumber + 1 }
                    
                    if (nextEpInSameSeason != null) {
                        val isRecent = (System.currentTimeMillis() - lastWatched.watchedAt) < 30L * 24 * 60 * 60 * 1000 // 30 days
                        val nextData = NextEpisodeData(
                            showId = show.showId,
                            showName = details.name ?: "Unknown",
                            backdropPath = details.backdrop_path,
                            posterPath = details.poster_path,
                            seasonNumber = lastWatched.seasonNumber,
                            episodeNumber = nextEpInSameSeason.episode_number,
                            episodeName = nextEpInSameSeason.name ?: "Episode ${nextEpInSameSeason.episode_number}"
                        )
                        if (isRecent) watchNext.add(nextData) else notWatchedForAWhile.add(nextData)
                    } else {
                        // Check next season
                        if (!seasonDetailsMap.containsKey("${show.showId}_${lastWatched.seasonNumber + 1}")) {
                            val seasonResult2 = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, lastWatched.seasonNumber + 1)
                            val seasonInfo2 = seasonResult2.getOrNull()
                            if (seasonInfo2 != null) {
                                seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber + 1}"] = seasonInfo2.episodes
                            }
                        }
                        val nextSeasonEpisodes = seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber + 1}"] ?: emptyList()
                        val firstEpNextSeason = nextSeasonEpisodes.find { it.episode_number == 1 }
                        if (firstEpNextSeason != null) {
                            val isRecent = (System.currentTimeMillis() - lastWatched.watchedAt) < 30L * 24 * 60 * 60 * 1000
                            val nextData = NextEpisodeData(
                                showId = show.showId,
                                showName = details.name ?: "Unknown",
                                backdropPath = details.backdrop_path,
                                posterPath = details.poster_path,
                                seasonNumber = lastWatched.seasonNumber + 1,
                                episodeNumber = 1,
                                episodeName = firstEpNextSeason.name ?: "Episode 1"
                            )
                            if (isRecent) watchNext.add(nextData) else notWatchedForAWhile.add(nextData)
                        }
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

                // Sort history descending
                watchedHistory.sortByDescending { it.watchedAt }

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
