import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

# Add missing import for Comment
if "import com.example.data.firebase.Comment" not in content:
    content = "import com.example.data.firebase.Comment\n" + content

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)
