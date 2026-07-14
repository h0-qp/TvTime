import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

replacement = """                    // Fetch season details if not cached
                    if (!seasonDetailsMap.containsKey("${show.showId}_${lastWatched.seasonNumber}")) {
                        val seasonResult = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, lastWatched.seasonNumber)
                        val seasonInfo = seasonResult.getOrNull()
                        if (seasonInfo != null) {
                            seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber}"] = seasonInfo.episodes
                        }
                    }"""

content = content.replace("""                    // Fetch season details if not cached
                    if (!seasonDetailsMap.containsKey("${show.showId}_${lastWatched.seasonNumber}")) {
                        repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, lastWatched.seasonNumber).getOrNull()?.let { seasonInfo ->
                            seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber}"] = seasonInfo.episodes
                        }
                    }""", replacement)

replacement2 = """                        if (!seasonDetailsMap.containsKey("${show.showId}_${lastWatched.seasonNumber + 1}")) {
                            val seasonResult2 = repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, lastWatched.seasonNumber + 1)
                            val seasonInfo2 = seasonResult2.getOrNull()
                            if (seasonInfo2 != null) {
                                seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber + 1}"] = seasonInfo2.episodes
                            }
                        }"""
content = content.replace("""                        if (!seasonDetailsMap.containsKey("${show.showId}_${lastWatched.seasonNumber + 1}")) {
                            repository.getSeasonDetails(apiKey, show.showId.toIntOrNull() ?: 0, lastWatched.seasonNumber + 1).getOrNull()?.let { seasonInfo ->
                                seasonDetailsMap["${show.showId}_${lastWatched.seasonNumber + 1}"] = seasonInfo.episodes
                            }
                        }""", replacement2)

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'w') as f:
    f.write(content)
