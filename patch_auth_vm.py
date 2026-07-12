import re

with open('app/src/main/java/com/example/ui/screens/auth/AuthViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''    fun signInWithGoogle(idToken: String) {''',
'''    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signInAnonymously()
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "فشل المتابعة كضيف")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {'''
)

with open('app/src/main/java/com/example/ui/screens/auth/AuthViewModel.kt', 'w') as f:
    f.write(content)

