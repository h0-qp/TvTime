package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import com.squareup.moshi.Json

interface TmdbApi {
    @GET("trending/tv/day")
    suspend fun getTrendingTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): MediaResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): MediaResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("tv/on_the_air")
    suspend fun getUpcomingTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): GenreResponse

    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): GenreResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("with_genres") withGenres: String? = null,
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("with_genres") withGenres: String? = null,
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MediaResponse

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "credits,similar,videos,watch/providers"
    ): MediaItem

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "credits,similar,videos,watch/providers"
    ): MediaItem
    
    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
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
    val overview: String? = null,
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
    val status: String? = null,
    val credits: Credits? = null,
    val similar: SimilarResponse? = null,
    val videos: VideosResponse? = null,
    val genres: List<Genre>? = null,
    val vote_average: Double? = null,
    val runtime: Int? = null,
    val episode_run_time: List<Int>? = null
,
    @Json(name = "watch/providers") val watch_providers: WatchProvidersResponse? = null)

data class EpisodeToAir(
    val id: Int,
    val name: String,
    val overview: String? = null,
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
    val overview: String? = null,
    val air_date: String? = null,
    val still_path: String?,
    val vote_average: Double? = null
)

data class SeasonDetails(
    val _id: String,
    val id: Int,
    val name: String,
    val episodes: List<Episode>
)


data class Credits(
    val cast: List<CastMember>
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profile_path: String?
)

data class SimilarResponse(
    val results: List<MediaItem>
)

data class VideosResponse(
    val results: List<VideoItem>
)

data class GenreResponse(
    val genres: List<Genre>
)

data class VideoItem(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

data class Genre(val id: Int, val name: String)
data class WatchProvidersResponse(
    val results: Map<String, WatchProviderRegion>?
)

data class WatchProviderRegion(
    val link: String?,
    val flatrate: List<WatchProviderItem>?,
    val rent: List<WatchProviderItem>?,
    val buy: List<WatchProviderItem>?
)

data class WatchProviderItem(
    val provider_id: Int,
    val provider_name: String,
    val logo_path: String?
)
