import re
file_path = "app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                // Sort history descending
                watchedHistory.sortWith(compareByDescending<WatchedEpisodeData> { it.watchedAt }
                    .thenByDescending { it.seasonNumber }
                    .thenByDescending { it.episodeNumber })"""

replacement = """                // Sort history ascending
                watchedHistory.sortWith(compareBy<WatchedEpisodeData> { it.watchedAt }
                    .thenBy { it.seasonNumber }
                    .thenBy { it.episodeNumber })"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
