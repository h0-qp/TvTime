import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Add missing @Composable for EpisodeDetailsContent if not present
if "fun EpisodeDetailsContent(" in content and "@Composable" not in content.split("fun EpisodeDetailsContent(")[0].split("\n")[-2]:
    content = content.replace("fun EpisodeDetailsContent(", "@Composable\nfun EpisodeDetailsContent(")

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
