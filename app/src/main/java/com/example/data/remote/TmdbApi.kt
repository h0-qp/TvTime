package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

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

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaResponse

    @GET("tv/on_the_air")
    suspend fun getUpcomingTvShows(
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
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaItem

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaItem
    
    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): SeasonDetails
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
    val release_date: String?,
    val seasons: List<Season>? = null,
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null,
    val next_episode_to_air: EpisodeToAir? = null,
    val last_episode_to_air: EpisodeToAir? = null,
    val status: String? = null
)

data class EpisodeToAir(
    val id: Int,
    val name: String,
    val overview: String,
    val air_date: String,
    val episode_number: Int,
    val season_number: Int,
    val still_path: String?
)

data class Season(
    val id: Int,
    val name: String,
    val season_number: Int,
    val episode_count: Int,
    val poster_path: String?
)

data class Episode(
    val id: Int,
    val name: String,
    val episode_number: Int,
    val season_number: Int,
    val overview: String,
    val still_path: String?
)

data class SeasonDetails(
    val _id: String,
    val id: Int,
    val name: String,
    val episodes: List<Episode>
)
