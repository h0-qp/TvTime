package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllMoviesViewModel(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<FirestoreMediaItem>>(emptyList())
    val movies: StateFlow<List<FirestoreMediaItem>> = _movies

    init {
        viewModelScope.launch {
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val movieList = mediaList.filter { it.mediaType == "movie" }.sortedByDescending { it.addedAt }
                _movies.value = movieList
            }
        }
    }
}
