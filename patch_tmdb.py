import re

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-AE"
    ): MediaResponse''',
'''    @GET("trending/movie/day")
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
    ): MediaResponse'''
)

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'w') as f:
    f.write(content)

