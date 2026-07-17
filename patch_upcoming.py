import re

with open("app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt", "r") as f:
    content = f.read()

old_code = """                val deferredUpcoming = tvMedia.map { media ->
                    async {
                        var epData: UpcomingEpisodeData? = null
                        val details = detailsCache[media.id.toString()] ?: repository.getMediaDetails(apiKey, media.id, "tv").getOrNull()
                        if (details != null) {"""

new_code = """                val deferredUpcoming = tvMedia.map { media ->
                    async {
                        var epData: UpcomingEpisodeData? = null
                        var details = detailsCache[media.id.toString()]
                        if (details == null) {
                            details = repository.getMediaDetails(apiKey, media.id, "tv").getOrNull()
                            if (details != null) {
                                detailsCache[media.id.toString()] = details
                            }
                        }
                        if (details != null) {"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt", "w") as f:
    f.write(content)
