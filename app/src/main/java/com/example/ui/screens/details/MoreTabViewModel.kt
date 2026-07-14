package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaStats(
    val platforms: Map<String, Int> = emptyMap(),
    val ratings: Map<String, Int> = emptyMap(),
    val emotions: Map<String, Int> = emptyMap(),
    val favorite_characters: Map<String, Int> = emptyMap(),
    val totalVotesPlatforms: Int = 0,
    val totalVotesRatings: Int = 0,
    val totalVotesEmotions: Int = 0,
    val totalVotesCharacters: Int = 0
)

data class UserVotes(
    val platform: String? = null,
    val rating: String? = null,
    val emotion: String? = null,
    val favorite_character: String? = null
)

data class MoreTabUiState(
    val stats: MediaStats = MediaStats(),
    val userVotes: UserVotes = UserVotes(),
    val isLoading: Boolean = true
)

class MoreTabViewModel(private val mediaId: String) : ViewModel() {
    private val _uiState = MutableStateFlow(MoreTabUiState())
    val uiState: StateFlow<MoreTabUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid

    init {
        observeStats()
        observeUserVotes()
    }

    private fun observeStats() {
        db.collection("media_stats").document(mediaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val stats = snapshot?.toObject(MediaStats::class.java) ?: MediaStats()
                _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
            }
    }

    private fun observeUserVotes() {
        val currentUid = uid ?: return
        db.collection("media_stats").document(mediaId)
            .collection("user_votes").document(currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val votes = snapshot?.toObject(UserVotes::class.java) ?: UserVotes()
                _uiState.value = _uiState.value.copy(userVotes = votes)
            }
    }

    fun votePlatform(platformId: String) {
        vote("platform", "platforms", "totalVotesPlatforms", platformId)
    }

    fun voteRating(ratingId: String) {
        vote("rating", "ratings", "totalVotesRatings", ratingId)
    }

    fun voteEmotion(emotionId: String) {
        vote("emotion", "emotions", "totalVotesEmotions", emotionId)
    }

    fun voteCharacter(characterId: String) {
        vote("favorite_character", "favorite_characters", "totalVotesCharacters", characterId)
    }

    private fun vote(
        voteField: String,
        statsMapField: String,
        totalField: String,
        newValue: String
    ) {
        val currentUid = uid ?: return
        val currentUserVotes = _uiState.value.userVotes
        val oldValue = when (voteField) {
            "platform" -> currentUserVotes.platform
            "rating" -> currentUserVotes.rating
            "emotion" -> currentUserVotes.emotion
            "favorite_character" -> currentUserVotes.favorite_character
            else -> null
        }

        if (oldValue == newValue) return // Already voted for this

        val statsRef = db.collection("media_stats").document(mediaId)
        val userVoteRef = statsRef.collection("user_votes").document(currentUid)

        db.runTransaction { transaction ->
            // Update user vote
            transaction.set(
                userVoteRef,
                mapOf(voteField to newValue),
                SetOptions.merge()
            )

            // Update stats
            if (oldValue != null) {
                // Decrement old value
                transaction.set(
                    statsRef,
                    mapOf(
                        "$statsMapField.$oldValue" to FieldValue.increment(-1)
                    ),
                    SetOptions.merge()
                )
            } else {
                // Increment total if this is a new vote category for the user
                transaction.set(
                    statsRef,
                    mapOf(
                        totalField to FieldValue.increment(1)
                    ),
                    SetOptions.merge()
                )
            }

            // Increment new value
            transaction.set(
                statsRef,
                mapOf(
                    "$statsMapField.$newValue" to FieldValue.increment(1)
                ),
                SetOptions.merge()
            )
        }
    }
}

class MoreTabViewModelFactory(
    private val mediaId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoreTabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoreTabViewModel(mediaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
