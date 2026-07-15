package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.FirestoreRepository
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllTvShowsViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _tvShows = MutableStateFlow<List<FirestoreMediaItem>>(emptyList())
    val tvShows: StateFlow<List<FirestoreMediaItem>> = _tvShows

    private val _tmdbDetails = MutableStateFlow<Map<Int, MediaItem>>(emptyMap())
    val tmdbDetails: StateFlow<Map<Int, MediaItem>> = _tmdbDetails

    init {
        viewModelScope.launch {
            firestoreRepository.observeUserMedia().collectLatest { mediaList ->
                val tvList = mediaList.filter { it.mediaType == "tv" }.sortedByDescending { it.addedAt }
                _tvShows.value = tvList
                
                val apiKey = BuildConfig.TMDB_API_KEY
                if (apiKey.isNotEmpty() && apiKey != "MY_TMDB_API_KEY") {
                    tvList.forEach { item ->
                        if (!_tmdbDetails.value.containsKey(item.id)) {
                            launch {
                                mediaRepository.getMediaDetails(apiKey, item.id, "tv").onSuccess { details ->
                                    _tmdbDetails.value = _tmdbDetails.value + (item.id to details)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
