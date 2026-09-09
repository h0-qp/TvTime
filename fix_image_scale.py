import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

new_logic = """
            try {
                // Downscale and compress image to Base64 string
                val options = android.graphics.BitmapFactory.Options()
                options.inJustDecodeBounds = true
                android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                
                // Calculate inSampleSize
                var inSampleSize = 1
                val reqWidth = 800
                val reqHeight = 800
                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                    val halfHeight: Int = options.outHeight / 2
                    val halfWidth: Int = options.outWidth / 2
                    while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                        inSampleSize *= 2
                    }
                }
                
                options.inJustDecodeBounds = false
                options.inSampleSize = inSampleSize
                
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
                val compressedBytes = outputStream.toByteArray()
                val base64String = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.DEFAULT)
                
                val dataUri = "data:image/jpeg;base64,$base64String"
                val finalComment = comment.copy(imageUrl = dataUri)
                saveCommentToFirestore(finalComment, continuation)
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
"""

# Replace the inner try-catch block
content = re.sub(r'try \{.*?continuation\.resume\(Result\.failure\(e\)\)\s*\}', new_logic.strip(), content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
