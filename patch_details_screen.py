import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Add a state for showing comments
state_code = """
    var showComments by remember { mutableStateOf(false) }
    val context = LocalContext.current
"""

# Find the start of DetailsScreen composable body
screen_start_pattern = r'fun DetailsScreen\(.*?\) \{'
match = re.search(screen_start_pattern, content, re.DOTALL)
if match:
    # insert state code right after the match
    end = match.end()
    # verify if showComments is not there
    if "var showComments by remember" not in content:
        content = content[:end] + "\n" + state_code + content[end:]

# Replace the Comments Section for Media (around line 872)
old_media_comments = """Text(text = "التعليقات", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "لا توجد تعليقات متاحة حالياً.", color = TextSecondary, fontSize = 14.sp)"""

new_media_comments = """Row(
            modifier = Modifier.fillMaxWidth().clickable { showComments = true }.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text(text = "التعليقات", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }"""

content = content.replace(old_media_comments, new_media_comments)

# We also need to add the CommentsScreenFullScreen at the end of the DetailsScreen box or scaffold
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

# The DetailsScreen composable ends with a Spacer or just the end of the Box.
# Let's just put it before the last brace of DetailsScreen.
# Wait, it's safer to find the end of DetailsScreen Box.
# Since we know `val comments = ...` is in DetailsScreen, we can find `if (uiState is DetailsUiState.Error)` and put it just before the `}` closing the main Box.
# Wait, let's find `fun EpisodeDetailsContent` and insert it before that.
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
