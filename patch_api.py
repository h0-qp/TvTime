import re

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''data class MediaItem(
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
    val number_of_episodes: Int? = null
)''',
'''data class MediaItem(
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
)'''
)

with open('app/src/main/java/com/example/data/remote/TmdbApi.kt', 'w') as f:
    f.write(content)

