import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

new_methods = """
    fun observeCommentsForMedia(mediaId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("comments")
            .whereEqualTo("mediaId", mediaId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        val comment = doc.toObject(Comment::class.java)
                        comment?.copy(id = doc.id)
                    }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: Comment, imageBytes: ByteArray? = null): Result<Unit> = suspendCoroutine { continuation ->
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("comment_images/${java.util.UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putBytes(imageBytes)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val finalComment = comment.copy(imageUrl = uri.toString())
                    saveCommentToFirestore(finalComment, continuation)
                }.addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
            }.addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
        } else {
            saveCommentToFirestore(comment, continuation)
        }
    }

    private fun saveCommentToFirestore(comment: Comment, continuation: kotlin.coroutines.Continuation<Result<Unit>>) {
        val ref = db.collection("comments").document()
        val commentWithId = comment.copy(id = ref.id)
        ref.set(commentWithId)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }
"""

content = content.replace("}", new_methods + "\n}", 1)

# we need to replace the LAST closing brace with the new methods + closing brace.
last_brace_index = content.rfind("}")
if last_brace_index != -1:
    content = content[:last_brace_index] + new_methods + "\n}"

# also add import for kotlin.coroutines.suspendCoroutine
if "import kotlin.coroutines.suspendCoroutine" not in content:
    content = content.replace("import com.google.firebase.firestore.FirebaseFirestore", "import com.google.firebase.firestore.FirebaseFirestore\nimport kotlin.coroutines.suspendCoroutine\nimport kotlin.coroutines.resume")

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
