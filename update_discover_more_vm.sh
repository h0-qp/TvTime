#!/bin/bash
sed -i 's/val error: String? = null/val error: String? = null,\n    val watchlistIds: Set<Int> = emptySet()/g' app/src/main/java/com/example/ui/screens/explore/DiscoverMoreViewModel.kt

sed -i 's/class DiscoverMoreViewModel(private val repository: MediaRepository)/class DiscoverMoreViewModel(\n    private val firestoreRepository: com.example.data.firebase.FirestoreRepository,\n    private val repository: MediaRepository\n)/g' app/src/main/java/com/example/ui/screens/explore/DiscoverMoreViewModel.kt

sed -i 's/class DiscoverMoreViewModelFactory(private val repository: MediaRepository)/class DiscoverMoreViewModelFactory(\n    private val firestoreRepository: com.example.data.firebase.FirestoreRepository,\n    private val repository: MediaRepository\n)/g' app/src/main/java/com/example/ui/screens/explore/DiscoverMoreViewModel.kt

sed -i 's/return DiscoverMoreViewModel(repository) as T/return DiscoverMoreViewModel(firestoreRepository, repository) as T/g' app/src/main/java/com/example/ui/screens/explore/DiscoverMoreViewModel.kt
