import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("viewModel.markEpisodeUnwatched(it.showId, it.seasonNumber, it.episodeNumber)", "viewModel.markEpisodeUnwatched(ep.showId, ep.seasonNumber, ep.episodeNumber)")
content = content.replace("viewModel.markEpisodeWatched(it.showId, it.seasonNumber, it.episodeNumber)", "viewModel.markEpisodeWatched(ep.showId, ep.seasonNumber, ep.episodeNumber)")
content = content.replace("viewModel.markEpisodeWatched(it.showId, 1, 1)", "viewModel.markEpisodeWatched(show.showId, 1, 1)")

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'w') as f:
    f.write(content)
