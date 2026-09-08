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
                tmdbApi.getTvDetails(id, apiKey)
            } else {
                tmdbApi.getMovieDetails(id, apiKey)
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
