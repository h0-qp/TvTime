import re

with open('app/src/main/java/com/example/data/repository/MediaRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''    suspend fun getTrendingMovies(apiKey: String): Result<MediaResponse> {
        return try {
            val response = tmdbApi.getTrendingMovies(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }''',
'''    suspend fun getTrendingMovies(apiKey: String): Result<MediaResponse> {
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
    }'''
)

with open('app/src/main/java/com/example/data/repository/MediaRepository.kt', 'w') as f:
    f.write(content)

