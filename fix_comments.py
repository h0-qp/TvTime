import re

# Fix FirestoreRepository.kt
with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

# Replace addComment to use base64 instead of Firebase Storage
add_comment_new = """    suspend fun addComment(comment: Comment, imageBytes: ByteArray? = null): Result<Unit> = suspendCoroutine { continuation ->
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            try {
                // Compress image to Base64 string
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val outputStream = java.io.ByteArrayOutputStream()
                // Compress to JPEG with 50% quality to save space
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, outputStream)
                val compressedBytes = outputStream.toByteArray()
                val base64String = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.DEFAULT)
                
                // Prefix with data URI scheme so Coil can load it natively
                val dataUri = "data:image/jpeg;base64,$base64String"
                val finalComment = comment.copy(imageUrl = dataUri)
                saveCommentToFirestore(finalComment, continuation)
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        } else {
            saveCommentToFirestore(comment, continuation)
        }
    }"""

# Since addComment is duplicated for some reason, we'll replace all occurrences of it.
# First, remove the old addComment block using a regex.
content = re.sub(r'suspend fun addComment\(comment: Comment, imageBytes: ByteArray\? = null\): Result<Unit> = suspendCoroutine \{ continuation ->.*?saveCommentToFirestore\(comment, continuation\)\s*\}\s*\}', add_comment_new, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)

# Fix CommentsSection.kt
with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'r') as f:
    content = f.read()

# Fix the FAB position in CommentsScreenFullScreen
content = content.replace('padding(paddingValues)', 'padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding() + 80.dp)')
# Change Scaffold to use windowInsets to respect nav bar
content = content.replace('Scaffold(', 'Scaffold(contentWindowInsets = WindowInsets.navigationBars,')
content = content.replace('DialogProperties(usePlatformDefaultWidth = false)', 'DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)')
# Make sure Dialog modifier has navigationBarsPadding and imePadding
content = content.replace('modifier = Modifier\n                    .fillMaxSize()', 'modifier = Modifier\n                    .fillMaxSize()\n                    .navigationBarsPadding()\n                    .imePadding()')

with open('app/src/main/java/com/example/ui/screens/details/CommentsSection.kt', 'w') as f:
    f.write(content)
