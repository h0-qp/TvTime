package com.example.ui.screens.person

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.remote.PersonDetails
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PersonUiState {
    object Loading : PersonUiState()
    data class Success(val person: PersonDetails) : PersonUiState()
    data class Error(val message: String) : PersonUiState()
}

class PersonViewModel(
    private val repository: MediaRepository,
    private val personId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonUiState>(PersonUiState.Loading)
    val uiState: StateFlow<PersonUiState> = _uiState

    init {
        loadPersonDetails()
    }

    private fun loadPersonDetails() {
        viewModelScope.launch {
            _uiState.value = PersonUiState.Loading
            val result = repository.getPersonDetails(BuildConfig.TMDB_API_KEY, personId)
            result.onSuccess { person ->
                _uiState.value = PersonUiState.Success(person)
            }.onFailure { error ->
                _uiState.value = PersonUiState.Error(error.localizedMessage ?: "Unknown Error")
            }
        }
    }
}

class PersonViewModelFactory(
    private val repository: MediaRepository,
    private val personId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonViewModel(repository, personId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
