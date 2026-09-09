import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Fix missing onCommentsClick in DetailsScreen when calling EpisodeDetailsContent
content = content.replace("episode = state.selectedEpisodeDetails,\n                            isWatched", "episode = state.selectedEpisodeDetails,\n                            onCommentsClick = { showComments = true },\n                            isWatched")

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
