import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('ep.timestamp', 'ep.watchedAt')
content = content.replace('lastWatched.timestamp', 'lastWatched.watchedAt')
content = content.replace('repository.getTvSeasonDetails', 'repository.getSeasonDetails')

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'w') as f:
    f.write(content)
