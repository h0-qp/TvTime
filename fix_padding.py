import re

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()

# Add padding to FloatingActionButton
content = content.replace(
    'FloatingActionButton(\n                    onClick = { showAddDialog = true },\n                    containerColor = Color.White',
    'FloatingActionButton(\n                    modifier = Modifier.padding(bottom = 32.dp, start = 16.dp),\n                    onClick = { showAddDialog = true },\n                    containerColor = Color.White'
)

# Add padding to AddComment bottom bar Row
content = content.replace(
    'Row(\n                    modifier = Modifier.fillMaxWidth().padding(16.dp),\n                    horizontalArrangement = Arrangement.End',
    'Row(\n                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp),\n                    horizontalArrangement = Arrangement.End'
)

# Also let's fix the AsyncImage to display base64. Coil should handle "data:image..." out of the box if configured, but let's be safe.
# Actually, Coil handles `data:` URIs natively without any extra config.

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)
