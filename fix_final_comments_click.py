import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Replace any lingering `onCommentsClick()` with `showComments = true` inside the DetailsScreen.
# Make sure to not touch `onCommentsClick = { showComments = true }` that might be an argument to EpisodeDetailsContent.
# Actually, the error says:
# e: file:///app/applet/app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt:195:49 Unresolved reference 'onCommentsClick'.
# e: file:///app/applet/app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt:896:60 Unresolved reference 'onCommentsClick'.

content = content.replace("onCommentsClick = { onCommentsClick() }", "onCommentsClick = { showComments = true }")
content = content.replace(".clickable { onCommentsClick() }", ".clickable { showComments = true }")

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
