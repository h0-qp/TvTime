import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# DetailsScreen composable body check
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

# the line is 903:    }
# 904:
# 905:
# 906:
# 907:}
# 908:fun EpisodeDetailsContent(

# We insert the block before line 907.
lines = content.split('\n')
for i, line in enumerate(lines):
    if "fun EpisodeDetailsContent" in line:
        # found EpisodeDetailsContent. We go backwards to find the closing brace of DetailsScreen.
        for j in range(i-1, -1, -1):
            if lines[j].strip() == "}":
                # this is the closing brace of DetailsScreen
                lines.insert(j, full_screen_comments)
                break
        break

content = "\n".join(lines)

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
