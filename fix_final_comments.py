import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Let's remove the block we inserted wrongly previously
full_screen_comments = """
    if (showComments) {
        val comments = (uiState as? DetailsUiState.Success)?.comments ?: emptyList()
        CommentsScreenFullScreen(
            comments = comments,
            onClose = { showComments = false },
            onAddComment = { text, uri, isGif ->
                val bytes = uri?.let { u ->
                    context.contentResolver.openInputStream(u)?.readBytes()
                }
                viewModel.addComment(text, bytes, isGif)
            }
        )
    }
"""

if full_screen_comments in content:
    content = content.replace(full_screen_comments, "")

# Find where to put it properly. Inside DetailsScreen composable.
# DetailsScreen usually ends after calling AboutTabContent or similar.
# Let's find "if (showBottomSheet) {" and insert it before that, because that's still inside DetailsScreen and has access to `showComments`, `uiState`, `viewModel`, `context`.
idx = content.find("if (showBottomSheet) {")
if idx != -1:
    content = content[:idx] + full_screen_comments + "\n" + content[idx:]
else:
    # If not found, let's find the closing brace of the main layout, e.g., the last Spacer or something inside DetailsScreen
    idx = content.find("if (uiState is DetailsUiState.Error) {")
    if idx != -1:
        content = content[:idx] + full_screen_comments + "\n" + content[idx:]


with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
