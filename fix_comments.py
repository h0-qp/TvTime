import re

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()

# Fix dialog close on publish
old_publish = """                            .clickable {
                                if (text.isNotBlank() || selectedImageUri != null) {
                                    onPublish(text, selectedImageUri, false)
                                }
                            }"""
new_publish = """                            .clickable {
                                if (text.isNotBlank() || selectedImageUri != null) {
                                    onPublish(text, selectedImageUri, false)
                                    onClose()
                                }
                            }"""
content = content.replace(old_publish, new_publish)

# Fix dialog layout (icons pushed down)
old_scaffold = """    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(contentWindowInsets = WindowInsets.navigationBars,
            containerColor = TrueBlack,
            topBar = {"""

new_scaffold = """    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            contentWindowInsets = WindowInsets.navigationBars,
            containerColor = TrueBlack,
            topBar = {"""

content = content.replace(old_scaffold, new_scaffold)

old_bottombar = """            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {"""
new_bottombar = """            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {"""

content = content.replace(old_bottombar, new_bottombar)


with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)
