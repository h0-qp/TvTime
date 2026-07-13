#!/bin/bash
sed -i 's/val activityItems: List<MediaItem> = emptyList(),/val activityItems: List<MediaItem> = emptyList(),\n        val watchlistIds: Set<Int> = emptySet(),/g' app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

sed -i 's/import kotlinx.coroutines.flow.StateFlow/import kotlinx.coroutines.flow.StateFlow\nimport com.example.data.firebase.FirestoreRepository\nimport com.example.data.firebase.FirestoreMediaItem\nimport kotlinx.coroutines.flow.collectLatest/g' app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

sed -i 's/class ExploreViewModel(/class ExploreViewModel(\n    private val firestoreRepository: FirestoreRepository,/g' app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

sed -i 's/class ExploreViewModelFactory(/class ExploreViewModelFactory(\n    private val firestoreRepository: FirestoreRepository,/g' app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

sed -i 's/return ExploreViewModel(repository) as T/return ExploreViewModel(firestoreRepository, repository) as T/g' app/src/main/java/com/example/ui/screens/explore/ExploreViewModel.kt

