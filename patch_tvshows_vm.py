import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''            // First get TMDB items
            val result = repository.getTrendingTvShows(apiKey)''',
'''            // First get TMDB items
            val result = repository.getUpcomingTvShows(apiKey)'''
)

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'w') as f:
    f.write(content)

