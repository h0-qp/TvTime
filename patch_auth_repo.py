import re

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'r') as f:
    content = f.read()

auth_repo = """
    fun getCurrentUser(): com.google.firebase.auth.FirebaseUser? {
        return auth.currentUser
    }
"""

if "fun getCurrentUser()" not in content:
    content = content.replace("}", auth_repo + "\n}", 1)
    
    last_brace_index = content.rfind("}")
    if last_brace_index != -1:
        content = content[:last_brace_index] + auth_repo + "\n}"

with open('app/src/main/java/com/example/data/firebase/FirestoreRepository.kt', 'w') as f:
    f.write(content)
