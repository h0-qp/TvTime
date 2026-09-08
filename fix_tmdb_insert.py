import sys
content = open("app/src/main/java/com/example/data/remote/TmdbApi.kt").read()

content = content.replace("""    @GET("tv/{tv_id}/season/{season_number}")""", """    @GET("movie/{movie_id}")
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
    
    @GET("tv/{tv_id}/season/{season_number}")""")

open("app/src/main/java/com/example/data/remote/TmdbApi.kt", "w").write(content)
