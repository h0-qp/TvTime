import os

file_path = "app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

target = """.collectLatest { (watchlist, watchedEps, allMedia) ->
                val showDetailsMap = mutableMapOf<String, MediaItem>()
                val seasonDetailsMap = mutableMapOf<String, List<Episode>>() 

                // 1. Fetch details for watchlist (Watch Next, History)
                val deferredDetails = watchlist.map { show ->"""

replacement = """.collectLatest { (watchlist, watchedEps, allMedia) ->
                val showDetailsMap = mutableMapOf<String, MediaItem>()
                val seasonDetailsMap = mutableMapOf<String, List<Episode>>() 

                val tvMedia = allMedia.filter { it.mediaType == "tv" }
                val activeWatchlist = tvMedia.map { WatchlistShow(it.id.toString(), it.addedAt) }

                // 1. Fetch details for watchlist (Watch Next, History)
                val deferredDetails = activeWatchlist.map { show ->"""

content = content.replace(target, replacement)

target2 = """for (show in watchlist) {"""
replacement2 = """for (show in activeWatchlist) {"""
content = content.replace(target2, replacement2)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Done")
