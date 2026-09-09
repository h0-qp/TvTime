import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

# Add comments to DetailsUiState
state_pattern = r'data class Success\((.*?)\) : DetailsUiState'
state_match = re.search(state_pattern, content, re.DOTALL)
if state_match:
    inner = state_match.group(1)
    if "val comments: List<com.example.data.firebase.Comment> = emptyList()" not in inner:
        new_inner = inner + ",\n        val comments: List<com.example.data.firebase.Comment> = emptyList()"
        content = content.replace(inner, new_inner)

# Add loadComments
init_pattern = r'private fun fetchDetails\(\) \{'

load_comments_code = """
    private fun observeComments() {
        viewModelScope.launch {
            firestoreRepository.observeCommentsForMedia(mediaId.toString()).collect { commentsList ->
                val currentState = _uiState.value
                if (currentState is DetailsUiState.Success) {
                    _uiState.value = currentState.copy(comments = commentsList)
                }
            }
        }
    }
"""

if "observeComments()" not in content:
    content = content.replace("private fun fetchDetails() {", load_comments_code + "\n    private fun fetchDetails() {\n        observeComments()")

add_comment_code = """
    fun addComment(text: String, imageBytes: ByteArray?, isGif: Boolean = false) {
        val currentState = _uiState.value
        if (currentState is DetailsUiState.Success) {
            viewModelScope.launch {
                val user = firestoreRepository.getCurrentUser()
                val comment = com.example.data.firebase.Comment(
                    mediaId = mediaId.toString(),
                    userId = user?.uid ?: "",
                    userName = user?.displayName ?: "Unknown User",
                    userProfileImage = user?.photoUrl,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    isGif = isGif
                )
                firestoreRepository.addComment(comment, imageBytes)
            }
        }
    }
"""

# we need to replace the LAST closing brace of the DetailsViewModel class with the new methods + closing brace.
# To do this safely, we find "class DetailsViewModelFactory".
factory_idx = content.find("class DetailsViewModelFactory")
if factory_idx != -1:
    before_factory = content[:factory_idx]
    after_factory = content[factory_idx:]
    last_brace_index = before_factory.rfind("}")
    if last_brace_index != -1:
        before_factory = before_factory[:last_brace_index] + add_comment_code + "\n}\n"
    content = before_factory + after_factory

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)
