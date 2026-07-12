import re

with open('app/src/main/java/com/example/data/firebase/AuthRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''    fun signOut() {''',
'''    suspend fun signInAnonymously(): Result<Unit> {
        return try {
            val authResult = kotlinx.coroutines.withTimeout(10000) {
                auth.signInAnonymously().await()
            }
            val user = authResult.user
            if (user != null) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "isAnonymous" to true
                )
                db.collection("users").document(user.uid).set(userMap)
            }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("انتهى وقت الاتصال. يرجى المحاولة مرة أخرى."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {'''
)

with open('app/src/main/java/com/example/data/firebase/AuthRepository.kt', 'w') as f:
    f.write(content)
