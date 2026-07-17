import re

with open("app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt", "r") as f:
    content = f.read()

# Add caches
content = content.replace(
    "val uiState: StateFlow<TvShowsUiState> = _uiState.asStateFlow()",
    "val uiState: StateFlow<TvShowsUiState> = _uiState.asStateFlow()\n\n    private val detailsCache = mutableMapOf<String, com.example.data.remote.MediaItem>()\n    private val seasonCache = mutableMapOf<String, List<com.example.data.remote.Episode>>()"
)

# Replace showDetailsMap and seasonDetailsMap initializations
content = content.replace(
    "val showDetailsMap = mutableMapOf<String, MediaItem>()\n                val lastWatchedMap = mutableMapOf<String, com.example.data.firebase.WatchedEpisode>()\n                val seasonDetailsMap = mutableMapOf<String, List<Episode>>()",
    "val lastWatchedMap = mutableMapOf<String, com.example.data.firebase.WatchedEpisode>()"
)

# Update deferredDetails
content = content.replace(
    "val deferredDetails = activeWatchlist.map { show ->",
    "val deferredDetails = activeWatchlist.filter { !detailsCache.containsKey(it.showId) }.map { show ->"
)

content = content.replace(
    "showDetailsMap[showId] = details",
    "detailsCache[showId] = details"
)

# Update season caching
content = content.replace(
    "val details = showDetailsMap[show.showId] ?: return@mapNotNull null",
    "val details = detailsCache[show.showId] ?: return@mapNotNull null"
)

content = content.replace(
"""                    if (firstUnwatchedSeason != null) {
                        async {
                            val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, firstUnwatchedSeason.season_number)
                            val seasonInfo = seasonResult.getOrNull()
                            Pair("${show.showId}_${firstUnwatchedSeason.season_number}", seasonInfo?.episodes)
                        }
                    } else {
                        null
                    }""",
"""                    if (firstUnwatchedSeason != null) {
                        val cacheKey = "${show.showId}_${firstUnwatchedSeason.season_number}"
                        if (seasonCache.containsKey(cacheKey)) {
                            null
                        } else {
                            async {
                                val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, firstUnwatchedSeason.season_number)
                                val seasonInfo = seasonResult.getOrNull()
                                Pair(cacheKey, seasonInfo?.episodes)
                            }
                        }
                    } else {
                        null
                    }"""
)

content = content.replace(
    "seasonDetailsMap[cacheKey] = episodes",
    "seasonCache[cacheKey] = episodes"
)

content = content.replace(
    "val details = showDetailsMap[show.showId] ?: continue",
    "val details = detailsCache[show.showId] ?: continue"
)

content = content.replace(
    "if (!seasonDetailsMap.containsKey(cacheKey)) {",
    "if (!seasonCache.containsKey(cacheKey)) {"
)

content = content.replace(
    "seasonDetailsMap[cacheKey] = seasonInfo.episodes",
    "seasonCache[cacheKey] = seasonInfo.episodes"
)

content = content.replace(
    "val seasonEpisodes = seasonDetailsMap[cacheKey] ?: emptyList()",
    "val seasonEpisodes = seasonCache[cacheKey] ?: emptyList()"
)

content = content.replace(
    "val details = showDetailsMap[media.id.toString()] ?: repository.getMediaDetails(apiKey, media.id, \"tv\").getOrNull()",
    "val details = detailsCache[media.id.toString()] ?: repository.getMediaDetails(apiKey, media.id, \"tv\").getOrNull()"
)

with open("app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt", "w") as f:
    f.write(content)
