import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

old_func = """    fun observeCommentsForMedia(mediaId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("comments")
            .whereEqualTo("mediaId", mediaId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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
    }"""

new_func = """    fun observeCommentsForMedia(mediaId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("comments")
            .whereEqualTo("mediaId", mediaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        val comment = doc.toObject(Comment::class.java)
                        comment?.copy(id = doc.id)
                    }.sortedByDescending { it.timestamp }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }"""

content = content.replace(old_func, new_func)

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
