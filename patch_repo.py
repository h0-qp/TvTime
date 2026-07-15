import re

with open('app/src/main/java/com/example/data/repository/MediaRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('suspend fun getMediaDetails',
'''    suspend fun getSeasonDetails(apiKey: String, tvId: Int, seasonNumber: Int): Result<com.example.data.remote.SeasonDetails> {
        return try {
            val response = tmdbApi.getSeasonDetails(tvId, seasonNumber, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMediaDetails''')

with open('app/src/main/java/com/example/data/repository/MediaRepository.kt', 'w') as f:
    f.write(content)

