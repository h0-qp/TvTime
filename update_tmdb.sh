cat << 'INNER_EOF' >> app/src/main/java/com/example/data/remote/TmdbApi.kt

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
INNER_EOF
