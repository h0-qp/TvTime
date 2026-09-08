import sys
content = open("app/src/main/java/com/example/data/remote/TmdbApi.kt").read()

# Fix getPersonDetails injection
import re
# Remove broken getPersonDetails and getMovieDetails
content = re.sub(r'    suspend fun getMovieDetails\([\s\S]*?    \): MediaItem', '', content)
content = re.sub(r'    @GET\("movie/\{movie_id\}"\)\s*    @GET\("person/\{person_id\}"\)\s*    suspend fun getPersonDetails\([\s\S]*?    \): PersonDetails', '', content)
content = re.sub(r'    @GET\("person/\{person_id\}"\)\s*    suspend fun getPersonDetails\([\s\S]*?    \): PersonDetails', '', content)
content = re.sub(r'    @GET\("movie/\{movie_id\}"\)\s*', '', content)

# Re-insert cleanly
replacement = """    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "credits,similar,videos,watch/providers"
    ): MediaItem

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar",
        @Query("append_to_response") appendToResponse: String = "combined_credits"
    ): PersonDetails

    @GET("tv/{tv_id}/season/{season_number}")"""

content = content.replace('    @GET("tv/{tv_id}/season/{season_number}")', replacement)

open("app/src/main/java/com/example/data/remote/TmdbApi.kt", "w").write(content)
