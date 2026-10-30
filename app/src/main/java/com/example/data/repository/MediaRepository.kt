package com.example.data.repository

import com.example.data.local.LocalMediaItem
import com.example.data.local.MediaDao
import com.example.data.remote.MediaResponse
import com.example.data.remote.TmdbApi
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val tmdbApi: TmdbApi,
    private val mediaDao: MediaDao
) {
    fun getLocalMediaByType(mediaType: String): Flow<List<LocalMediaItem>> {
        return mediaDao.getMediaItemsByType(mediaType)
    }

    suspend fun insertLocalMedia(item: LocalMediaItem) {
        mediaDao.insertMediaItem(item)
    }

    suspend fun deleteLocalMedia(id: Int) {
        mediaDao.deleteMediaItem(id)
    }

    suspend fun updateLocalMediaWatchedStatus(id: Int, isWatched: Boolean) {
        mediaDao.updateWatchedStatus(id, isWatched)
    }

    suspend fun getTrendingTvShows(apiKey: String): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getTrendingTvShows(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingMovies(apiKey: String): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getTrendingMovies(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingMovies(apiKey: String): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getUpcomingMovies(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingTvShows(apiKey: String): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getUpcomingTvShows(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularTvShows(apiKey: String, page: Int = 1): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getPopularTvShows(apiKey, page = page)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularMovies(apiKey: String, page: Int = 1): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getPopularMovies(apiKey, page = page)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieGenres(apiKey: String): Result<com.example.data.remote.GenreResponse> {
        return try {
            val response = tmdbApi.getMovieGenres(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTvGenres(apiKey: String): Result<com.example.data.remote.GenreResponse> {
        return try {
            val response = tmdbApi.getTvGenres(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun discoverMovies(apiKey: String, withGenres: String? = null, page: Int = 1): Result<MediaResponse> {
        return try {
            val response = tmdbApi.discoverMovies(apiKey, withGenres = withGenres, page = page)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun discoverTv(apiKey: String, withGenres: String? = null, page: Int = 1): Result<MediaResponse> {
        return try {
            val response = tmdbApi.discoverTv(apiKey, withGenres = withGenres, page = page)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMulti(apiKey: String, query: String): Result<MediaResponse> {
        return try {
            val response = tmdbApi.searchMulti(apiKey, query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        suspend fun getSeasonDetails(apiKey: String, tvId: Int, seasonNumber: Int): Result<com.example.data.remote.SeasonDetails> {
        return try {
            val response = tmdbApi.getSeasonDetails(tvId, seasonNumber, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCollectionDetails(apiKey: String, collectionId: Int): Result<com.example.data.remote.CollectionDetailsResponse> {
        return try {
            val response = tmdbApi.getCollectionDetails(collectionId, apiKey, "ar")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMediaDetails(apiKey: String, id: Int, mediaType: String): Result<com.example.data.remote.MediaItem> {
        return try {
            val response = if (mediaType == "tv") {
                val tvDetails = tmdbApi.getTvDetails(id, apiKey)
                ensureAnnouncedSeasons(tvDetails)
            } else {
                tmdbApi.getMovieDetails(id, apiKey)
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ensureAnnouncedSeasons(item: com.example.data.remote.MediaItem): com.example.data.remote.MediaItem {
        val seasons = item.seasons?.toMutableList() ?: mutableListOf()
        val regularSeasons = seasons.filter { it.season_number > 0 }
        val maxSeasonNum = regularSeasons.maxOfOrNull { it.season_number } ?: 0

        // 1. If number_of_seasons from TMDB is greater than max regular season in list, add missing seasons
        val reportedNumberOfSeasons = item.number_of_seasons ?: 0
        if (reportedNumberOfSeasons > maxSeasonNum) {
            for (s in (maxSeasonNum + 1)..reportedNumberOfSeasons) {
                if (seasons.none { it.season_number == s }) {
                    seasons.add(
                        com.example.data.remote.Season(
                            id = -s,
                            name = "الموسم $s",
                            season_number = s,
                            episode_count = 0,
                            poster_path = item.poster_path
                        )
                    )
                }
            }
        }

        val currentMaxSeason = seasons.filter { it.season_number > 0 }.maxOfOrNull { it.season_number } ?: 0

        // 2. Check if show is returning, in production, or planned
        val isReturningOrInProd = item.status?.trim()?.equals("Returning Series", ignoreCase = true) == true ||
                item.status?.trim()?.equals("In Production", ignoreCase = true) == true ||
                item.status?.trim()?.equals("Planned", ignoreCase = true) == true ||
                item.in_production == true

        if (isReturningOrInProd) {
            // Check if there is already an un-aired or announced season (e.g. season with 0 episodes or > currentMaxSeason)
            val hasAnnouncedOrZeroSeason = seasons.any { s ->
                s.season_number > 0 && (s.season_number > currentMaxSeason || s.episode_count == 0)
            }
            if (!hasAnnouncedOrZeroSeason) {
                val nextSeason = currentMaxSeason + 1
                seasons.add(
                    com.example.data.remote.Season(
                        id = -nextSeason,
                        name = "الموسم $nextSeason",
                        season_number = nextSeason,
                        episode_count = 0,
                        poster_path = item.poster_path
                    )
                )
            }
        }

        // 3. Check if next_episode_to_air has a season number not in seasons list
        val nextEpSeason = item.next_episode_to_air?.season_number
        if (nextEpSeason != null && nextEpSeason > 0 && seasons.none { it.season_number == nextEpSeason }) {
            seasons.add(
                com.example.data.remote.Season(
                    id = -nextEpSeason,
                    name = "الموسم $nextEpSeason",
                    season_number = nextEpSeason,
                    episode_count = 0,
                    poster_path = item.poster_path
                )
            )
        }

        val sortedSeasons = seasons.sortedBy { it.season_number }
        val newCount = sortedSeasons.filter { it.season_number > 0 }.size
        return item.copy(
            seasons = sortedSeasons,
            number_of_seasons = maxOf(item.number_of_seasons ?: 0, newCount)
        )
    }

    suspend fun getPersonDetails(apiKey: String, personId: Int): Result<com.example.data.remote.PersonDetails> {
        return try {
            val response = tmdbApi.getPersonDetails(personId, apiKey, "ar")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
