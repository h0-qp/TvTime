import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    lines = f.readlines()

# The comments logic is currently between line 891 and 903:
#    if (showComments) {
#        val comments = (uiState as? DetailsUiState.Success)?.comments ?: emptyList()
#        CommentsScreenFullScreen(
#            comments = comments,
# ...
#        )
#    }

new_lines = []
in_comments_block = False
comments_block_lines = []
for i, line in enumerate(lines):
    if "if (showComments) {" in line and "val comments =" in lines[i+1]:
        in_comments_block = True
    
    if in_comments_block:
        comments_block_lines.append(line)
        if line.strip() == "}" and len(comments_block_lines) > 2:
            # check if it closed the if statement
            if comments_block_lines.count(line) == sum(1 for x in comments_block_lines if '{' in x) - sum(1 for x in comments_block_lines if '}' in x) + 1:
                # wait, let's just use exact line numbers or regex
                pass
            # actually it's exactly 12 lines
            if len(comments_block_lines) == 13:
                in_comments_block = False
        continue

    new_lines.append(line)

content = "".join(new_lines)

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

# Now insert it at the end of DetailsScreen, around line 692 (before the last brace of DetailsScreen)
about_tab = "@Composable\nfun AboutTabContent"
idx = content.find(about_tab)
if idx != -1:
    before = content[:idx]
    after = content[idx:]
    # find the last '}' in before which closes DetailsScreen
    # it looks like:
    # 691:        }
    # 692:    }
    # 693:    }
    # 694:}
    
    last_brace = before.rfind("}")
    before = before[:last_brace] + full_screen_comments + "\n}\n"
    content = before + after

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)

