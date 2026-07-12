package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("trending/tv/day")
    suspend fun getTrendingTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaResponse

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "ar-AE",
        @Query("page") page: Int = 1
    ): MediaResponse
    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaItem

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaItem
}

data class MediaResponse(
    val page: Int,
    val results: List<MediaItem>,
    val total_pages: Int,
    val total_results: Int
)

data class MediaItem(
    val id: Int,
    val name: String?,
    val title: String?,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val media_type: String?,
    val first_air_date: String?,
    val release_date: String?
)
