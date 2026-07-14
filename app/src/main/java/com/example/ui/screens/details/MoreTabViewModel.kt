package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaStats(
    val platforms: Map<String, Long> = emptyMap(),
    val ratings: Map<String, Long> = emptyMap(),
    val emotions: Map<String, Long> = emptyMap(),
    val favorite_characters: Map<String, Long> = emptyMap(),
    val totalVotesPlatforms: Long = 0L,
    val totalVotesRatings: Long = 0L,
    val totalVotesEmotions: Long = 0L,
    val totalVotesCharacters: Long = 0L
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
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MoreTabViewModel(private val mediaId: String) : ViewModel() {
    private val _uiState = MutableStateFlow(MoreTabUiState())
    val uiState: StateFlow<MoreTabUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid

    private var votesListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        observeStats()
        setupAuthAndVotesObserver()
    }

    private fun observeStats() {
        db.collection("media_stats").document(mediaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                    return@addSnapshotListener
                }
                val stats = if (snapshot != null && snapshot.exists()) {
                    snapshot.toMediaStats()
                } else {
                    MediaStats()
                }
                _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
            }
    }

    private fun setupAuthAndVotesObserver() {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUid = firebaseAuth.currentUser?.uid
            votesListenerRegistration?.remove()
            if (currentUid != null) {
                votesListenerRegistration = db.collection("media_stats").document(mediaId)
                    .collection("user_votes").document(currentUid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("MoreTabViewModel", "Error listening to user votes", error)
                            _uiState.value = _uiState.value.copy(errorMessage = error.message)
                            return@addSnapshotListener
                        }
                        val votes = if (snapshot != null && snapshot.exists()) {
                            snapshot.toUserVotes()
                        } else {
                            UserVotes()
                        }
                        _uiState.value = _uiState.value.copy(userVotes = votes)
                    }
            } else {
                _uiState.value = _uiState.value.copy(userVotes = UserVotes())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        votesListenerRegistration?.remove()
    }

    fun votePlatform(platformId: String) {
        vote("platform", platformId)
    }

    fun voteRating(ratingId: String) {
        vote("rating", ratingId)
    }

    fun voteEmotion(emotionId: String) {
        vote("emotion", emotionId)
    }

    fun voteCharacter(characterId: String) {
        vote("favorite_character", characterId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun vote(voteField: String, newValue: String) {
        val currentUid = uid
        if (currentUid == null) {
            android.util.Log.e("MoreTabViewModel", "Cannot vote: User is not logged in!")
            return
        }

        val previousState = _uiState.value
        val currentVotes = previousState.userVotes
        val stats = previousState.stats

        val oldValue = when (voteField) {
            "platform" -> currentVotes.platform
            "rating" -> currentVotes.rating
            "emotion" -> currentVotes.emotion
            "favorite_character" -> currentVotes.favorite_character
            else -> null
        }

        if (oldValue == newValue) {
            return
        }

        // --- OPTIMISTIC UI UPDATE ---
        val newVotes = when (voteField) {
            "platform" -> currentVotes.copy(platform = newValue)
            "rating" -> currentVotes.copy(rating = newValue)
            "emotion" -> currentVotes.copy(emotion = newValue)
            "favorite_character" -> currentVotes.copy(favorite_character = newValue)
            else -> currentVotes
        }

        val updatedPlatforms = stats.platforms.toMutableMap()
        val updatedRatings = stats.ratings.toMutableMap()
        val updatedEmotions = stats.emotions.toMutableMap()
        val updatedCharacters = stats.favorite_characters.toMutableMap()

        var totalPlatforms = stats.totalVotesPlatforms
        var totalRatings = stats.totalVotesRatings
        var totalEmotions = stats.totalVotesEmotions
        var totalCharacters = stats.totalVotesCharacters

        when (voteField) {
            "platform" -> {
                if (oldValue != null) {
                    val currentCount = updatedPlatforms[oldValue] ?: 0L
                    updatedPlatforms[oldValue] = (currentCount - 1L).coerceAtLeast(0L)
                } else {
                    totalPlatforms += 1L
                }
                val newCount = updatedPlatforms[newValue] ?: 0L
                updatedPlatforms[newValue] = newCount + 1L
            }
            "rating" -> {
                if (oldValue != null) {
                    val currentCount = updatedRatings[oldValue] ?: 0L
                    updatedRatings[oldValue] = (currentCount - 1L).coerceAtLeast(0L)
                } else {
                    totalRatings += 1L
                }
                val newCount = updatedRatings[newValue] ?: 0L
                updatedRatings[newValue] = newCount + 1L
            }
            "emotion" -> {
                if (oldValue != null) {
                    val currentCount = updatedEmotions[oldValue] ?: 0L
                    updatedEmotions[oldValue] = (currentCount - 1L).coerceAtLeast(0L)
                } else {
                    totalEmotions += 1L
                }
                val newCount = updatedEmotions[newValue] ?: 0L
                updatedEmotions[newValue] = newCount + 1L
            }
            "favorite_character" -> {
                if (oldValue != null) {
                    val currentCount = updatedCharacters[oldValue] ?: 0L
                    updatedCharacters[oldValue] = (currentCount - 1L).coerceAtLeast(0L)
                } else {
                    totalCharacters += 1L
                }
                val newCount = updatedCharacters[newValue] ?: 0L
                updatedCharacters[newValue] = newCount + 1L
            }
        }

        val updatedStats = MediaStats(
            platforms = updatedPlatforms,
            ratings = updatedRatings,
            emotions = updatedEmotions,
            favorite_characters = updatedCharacters,
            totalVotesPlatforms = totalPlatforms,
            totalVotesRatings = totalRatings,
            totalVotesEmotions = totalEmotions,
            totalVotesCharacters = totalCharacters
        )

        // Apply local update instantly
        _uiState.value = previousState.copy(
            userVotes = newVotes,
            stats = updatedStats
        )

        // --- FIRESTORE TRANSACTION ---
        val statsRef = db.collection("media_stats").document(mediaId)
        val userVoteRef = statsRef.collection("user_votes").document(currentUid)

        db.runTransaction { transaction ->
            // 1. Read user vote inside transaction safely
            val userVoteDoc = transaction.get(userVoteRef)
            val currentVotesTx = if (userVoteDoc.exists()) userVoteDoc.toUserVotes() else UserVotes()

            val oldValueTx = when (voteField) {
                "platform" -> currentVotesTx.platform
                "rating" -> currentVotesTx.rating
                "emotion" -> currentVotesTx.emotion
                "favorite_character" -> currentVotesTx.favorite_character
                else -> null
            }

            if (oldValueTx == newValue) {
                return@runTransaction null // No change
            }

            // 2. Read current stats inside transaction safely
            val statsDoc = transaction.get(statsRef)
            val statsTx = if (statsDoc.exists()) statsDoc.toMediaStats() else MediaStats()

            // 3. Compute new stats in-memory inside transaction
            val updatedPlatformsTx = statsTx.platforms.toMutableMap()
            val updatedRatingsTx = statsTx.ratings.toMutableMap()
            val updatedEmotionsTx = statsTx.emotions.toMutableMap()
            val updatedCharactersTx = statsTx.favorite_characters.toMutableMap()

            var totalPlatformsTx = statsTx.totalVotesPlatforms
            var totalRatingsTx = statsTx.totalVotesRatings
            var totalEmotionsTx = statsTx.totalVotesEmotions
            var totalCharactersTx = statsTx.totalVotesCharacters

            when (voteField) {
                "platform" -> {
                    if (oldValueTx != null) {
                        val currentCount = updatedPlatformsTx[oldValueTx] ?: 0L
                        updatedPlatformsTx[oldValueTx] = (currentCount - 1L).coerceAtLeast(0L)
                    } else {
                        totalPlatformsTx += 1L
                    }
                    val newCount = updatedPlatformsTx[newValue] ?: 0L
                    updatedPlatformsTx[newValue] = newCount + 1L
                }
                "rating" -> {
                    if (oldValueTx != null) {
                        val currentCount = updatedRatingsTx[oldValueTx] ?: 0L
                        updatedRatingsTx[oldValueTx] = (currentCount - 1L).coerceAtLeast(0L)
                    } else {
                        totalRatingsTx += 1L
                    }
                    val newCount = updatedRatingsTx[newValue] ?: 0L
                    updatedRatingsTx[newValue] = newCount + 1L
                }
                "emotion" -> {
                    if (oldValueTx != null) {
                        val currentCount = updatedEmotionsTx[oldValueTx] ?: 0L
                        updatedEmotionsTx[oldValueTx] = (currentCount - 1L).coerceAtLeast(0L)
                    } else {
                        totalEmotionsTx += 1L
                    }
                    val newCount = updatedEmotionsTx[newValue] ?: 0L
                    updatedEmotionsTx[newValue] = newCount + 1L
                }
                "favorite_character" -> {
                    if (oldValueTx != null) {
                        val currentCount = updatedCharactersTx[oldValueTx] ?: 0L
                        updatedCharactersTx[oldValueTx] = (currentCount - 1L).coerceAtLeast(0L)
                    } else {
                        totalCharactersTx += 1L
                    }
                    val newCount = updatedCharactersTx[newValue] ?: 0L
                    updatedCharactersTx[newValue] = newCount + 1L
                }
            }

            val updatedStatsTx = MediaStats(
                platforms = updatedPlatformsTx,
                ratings = updatedRatingsTx,
                emotions = updatedEmotionsTx,
                favorite_characters = updatedCharactersTx,
                totalVotesPlatforms = totalPlatformsTx,
                totalVotesRatings = totalRatingsTx,
                totalVotesEmotions = totalEmotionsTx,
                totalVotesCharacters = totalCharactersTx
            )

            // 4. Perform the writes
            transaction.set(
                userVoteRef,
                mapOf(voteField to newValue),
                SetOptions.merge()
            )

            transaction.set(
                statsRef,
                updatedStatsTx,
                SetOptions.merge()
            )

            null
        }.addOnSuccessListener {
            android.util.Log.d("MoreTabViewModel", "Vote transaction succeeded for $voteField -> $newValue")
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }.addOnFailureListener { e ->
            android.util.Log.e("MoreTabViewModel", "Vote transaction failed, reverting UI state", e)
            _uiState.value = previousState.copy(errorMessage = e.message ?: "Unknown Firestore Error")
        }
    }
}

fun com.google.firebase.firestore.DocumentSnapshot.toMediaStats(): MediaStats {
    val platforms = (get("platforms") as? Map<*, *>)?.entries?.associate {
        it.key.toString() to ((it.value as? Number)?.toLong() ?: 0L)
    } ?: emptyMap()

    val ratings = (get("ratings") as? Map<*, *>)?.entries?.associate {
        it.key.toString() to ((it.value as? Number)?.toLong() ?: 0L)
    } ?: emptyMap()

    val emotions = (get("emotions") as? Map<*, *>)?.entries?.associate {
        it.key.toString() to ((it.value as? Number)?.toLong() ?: 0L)
    } ?: emptyMap()

    val favoriteCharacters = (get("favorite_characters") as? Map<*, *>)?.entries?.associate {
        it.key.toString() to ((it.value as? Number)?.toLong() ?: 0L)
    } ?: emptyMap()

    val totalVotesPlatforms = getLong("totalVotesPlatforms") ?: 0L
    val totalVotesRatings = getLong("totalVotesRatings") ?: 0L
    val totalVotesEmotions = getLong("totalVotesEmotions") ?: 0L
    val totalVotesCharacters = getLong("totalVotesCharacters") ?: 0L

    return MediaStats(
        platforms = platforms,
        ratings = ratings,
        emotions = emotions,
        favorite_characters = favoriteCharacters,
        totalVotesPlatforms = totalVotesPlatforms,
        totalVotesRatings = totalVotesRatings,
        totalVotesEmotions = totalVotesEmotions,
        totalVotesCharacters = totalVotesCharacters
    )
}

fun com.google.firebase.firestore.DocumentSnapshot.toUserVotes(): UserVotes {
    return UserVotes(
        platform = getString("platform"),
        rating = getString("rating"),
        emotion = getString("emotion"),
        favorite_character = getString("favorite_character")
    )
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
