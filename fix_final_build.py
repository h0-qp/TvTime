import re

# Fix FirestoreRepository.kt -> remove "private" from local function
with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()
if "private fun saveCommentToFirestore(" in content:
    content = content.replace("private fun saveCommentToFirestore(", "fun saveCommentToFirestore(")
with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)


# Fix CommentsSection.kt -> alpha
with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()
if "import androidx.compose.ui.draw.alpha" not in content:
    content = content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.alpha")
with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)


# Fix DetailsScreen.kt -> Unresolved reference 'showComments'
# We have a missing showComments inside EpisodeDetailsContent maybe?
# Ah: line 895 is probably EpisodeDetailsContent body where it uses onCommentsClick. But there might be another showComments reference.
with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Let's fix the @Composable annotation missing
if "fun EpisodeDetailsContent(" in content and "@Composable" not in content.split("fun EpisodeDetailsContent(")[0].split("\n")[-2]:
    content = content.replace("fun EpisodeDetailsContent(", "@Composable\nfun EpisodeDetailsContent(")

# Check where showComments is used
# 'showComments' is only declared in DetailsScreen but maybe we try to use it in AboutTabContent?
# No, "showComments" was probably used on line 895, let's see.
# In DetailsScreen, we added:
# if (showComments) { ... }
# Let's just make sure it's correct. Wait, if it complains about showComments, maybe we put the if (showComments) { ... } block OUTSIDE the DetailsScreen?
# Ah, I see: I put it before fun EpisodeDetailsContent. But did I put it inside the closing brace of DetailsScreen?
# The script `fix_details_screen_end.py` put it at the very end. Let's just remove it and put it properly inside DetailsScreen, before the last `}`.

details_match = re.search(r'fun DetailsScreen.*?\{', content, re.DOTALL)
if details_match:
    # We need to find the matching closing brace for DetailsScreen
    pass

