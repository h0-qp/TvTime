import re

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()

# Fix unresolved references in CommentsSection.kt
if "import com.example.data.firebase.Comment" not in content:
    content = content.replace("package com.example.ui.screens.details", "package com.example.ui.screens.details\n\nimport com.example.data.firebase.Comment")

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)
