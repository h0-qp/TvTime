import re

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()

old_fab = """            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 32.dp, start = 16.dp),"""
new_fab = """            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp),"""
content = content.replace(old_fab, new_fab)

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)
