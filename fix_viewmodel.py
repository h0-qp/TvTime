import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

old_add = """                firestoreRepository.addComment(comment, imageBytes)
            }
        }
    }"""
new_add = """                val result = firestoreRepository.addComment(comment, imageBytes)
                if (result.isFailure) {
                    val exception = result.exceptionOrNull()
                    android.util.Log.e("DetailsViewModel", "Failed to add comment", exception)
                }
            }
        }
    }"""
content = content.replace(old_add, new_add)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)
