import re

with open('app/src/main/java/com/example/ui/screens/movies/MoviesViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''            // First get TMDB items
            val result = repository.getTrendingMovies(apiKey)''',
'''            // First get TMDB items
            val result = repository.getUpcomingMovies(apiKey)'''
)

with open('app/src/main/java/com/example/ui/screens/movies/MoviesViewModel.kt', 'w') as f:
    f.write(content)

