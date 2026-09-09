import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# DetailsScreen composable body check
# It seems my previous script placed the state variables `showComments` outside the DetailsScreen composable, probably at the file level or something, 
# and it placed the CommentsScreenFullScreen in a bad place.

# Let's fix DetailsScreen.kt
# First, remove the previously added state_code if it's placed wrongly
state_code = """
    var showComments by remember { mutableStateOf(false) }
    val context = LocalContext.current
"""
if state_code in content:
    content = content.replace(state_code, "")

# Remove the previously added CommentsScreenFullScreen block
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

# Find the start of DetailsScreen composable body properly
# `fun DetailsScreen(`
# `    mediaId: Int,`
# `    mediaType: String,`
# ...
# `) {`
screen_start_pattern = r'fun DetailsScreen\(.*?\)\s*\{'
match = re.search(screen_start_pattern, content, re.DOTALL)
if match:
    # insert state code right after the match
    end = match.end()
    if "var showComments by remember" not in content:
        content = content[:end] + "\n" + state_code + content[end:]

# Add the CommentsScreenFullScreen inside DetailsScreen. Let's find the closing brace of DetailsScreen.
# Wait, `fun EpisodeDetailsContent` is right after `DetailsScreen`.
ep_details = "fun EpisodeDetailsContent"
ep_idx = content.find(ep_details)
if ep_idx != -1:
    before_ep = content[:ep_idx]
    after_ep = content[ep_idx:]
    
    # find the last brace before fun EpisodeDetailsContent which should be the end of DetailsScreen
    last_brace_idx = before_ep.rfind("}")
    if last_brace_idx != -1:
        before_ep = before_ep[:last_brace_idx] + full_screen_comments + "\n}\n"
    
    content = before_ep + after_ep

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
