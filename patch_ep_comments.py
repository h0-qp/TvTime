import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Add onCommentsClick parameter to EpisodeDetailsContent
if "onCommentsClick: () -> Unit" not in content:
    content = content.replace("fun EpisodeDetailsContent(\n    episode: com.example.data.remote.Episode,", "fun EpisodeDetailsContent(\n    episode: com.example.data.remote.Episode,\n    onCommentsClick: () -> Unit,")
    content = content.replace("fun EpisodeDetailsContent(\n    episode: com.example.data.remote.Episode,\n    isWatched: Boolean", "fun EpisodeDetailsContent(\n    episode: com.example.data.remote.Episode,\n    onCommentsClick: () -> Unit,\n    isWatched: Boolean")

    # In DetailsScreen, find where EpisodeDetailsContent is called
    content = content.replace("EpisodeDetailsContent(\n                                episode = it,", "EpisodeDetailsContent(\n                                episode = it,\n                                onCommentsClick = { showComments = true },")

    # Inside EpisodeDetailsContent, make the row clickable
    content = content.replace(".clickable { /* Expand / navigate to comments */ }", ".clickable { onCommentsClick() }")

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
