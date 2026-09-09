import re

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()

old_dialog = """    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            contentWindowInsets = WindowInsets.navigationBars,
            containerColor = TrueBlack,
            topBar = {"""

new_dialog = """    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            containerColor = TrueBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {"""

content = content.replace(old_dialog, new_dialog)

old_bottombar = """            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Gif,
                        contentDescription = "GIF",
                        tint = TextSecondary,
                        modifier = Modifier.size(32.dp).clickable { /* TODO: GIF picker */ }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image",
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp).clickable {
                            imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }"""

new_bottombar = """            bottomBar = {
                Column {
                    HorizontalDivider(color = DarkGrey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gif,
                            contentDescription = "GIF",
                            tint = TextSecondary,
                            modifier = Modifier.size(32.dp).clickable { /* TODO: GIF picker */ }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp).clickable {
                                imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                    }
                }
            }"""

content = content.replace(old_bottombar, new_bottombar)

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)
