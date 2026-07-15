import re

file_path = "app/src/main/java/com/example/data/firebase/FirestoreRepository.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """        val listener = db.collection("users").document(uid)
            .collection("watched_episodes")
            .orderBy("watchedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->"""

replacement = """        val listener = db.collection("users").document(uid)
            .collection("watched_episodes")
            .addSnapshotListener { snapshot, error ->"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success Repo Revert")
else:
    print("Target not found in Repo")
