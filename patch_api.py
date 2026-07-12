with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'r') as f:
    content = f.read()

content = content.replace('"en-US"', '"ar-AE"')

# Also add append_to_response
content = content.replace('@Query("language") language: String = "ar-AE"\n    ): MediaItem', '@Query("language") language: String = "ar-AE",\n        @Query("append_to_response") appendToResponse: String = "credits,similar,videos"\n    ): MediaItem')

# Also add Credits and Similar models
models = """
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

data class VideoItem(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)
"""

if "data class Credits" not in content:
    content = content + "\n" + models

# add credits, similar, videos to MediaItem
if "val credits: Credits?" not in content:
    content = content.replace('val status: String? = null', 'val status: String? = null,\n    val credits: Credits? = null,\n    val similar: SimilarResponse? = null,\n    val videos: VideosResponse? = null,\n    val genres: List<Genre>? = null,\n    val vote_average: Double? = null,\n    val runtime: Int? = null,\n    val episode_run_time: List<Int>? = null')

if "data class Genre" not in content:
    content = content + "\n" + "data class Genre(val id: Int, val name: String)"

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'w') as f:
    f.write(content)
