import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

replacement = """                    if (currentWatched.contains(episodeKey)) {
                        currentWatched.remove(episodeKey)
                        firestoreRepository.markEpisodeUnwatched(item.id.toString(), seasonNumber, episodeNumber)
                    } else {
                        currentWatched.add(episodeKey)
                        firestoreRepository.markEpisodeWatched(item.id.toString(), seasonNumber, episodeNumber)
                    }
                    firestoreRepository.addOrUpdateMedia(
                        firestoreItem.copy(watchedEpisodes = currentWatched)
                    )"""

content = content.replace("""                    if (currentWatched.contains(episodeKey)) {
                        currentWatched.remove(episodeKey)
                    } else {
                        currentWatched.add(episodeKey)
                    }
                    firestoreRepository.addOrUpdateMedia(
                        firestoreItem.copy(watchedEpisodes = currentWatched)
                    )""", replacement.replace('item.id.toString()', 'currentState.mediaItem.id.toString()'))

replacement2 = """                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = false,
                            watchedEpisodes = listOf(episodeKey),
                            addedAt = System.currentTimeMillis()
                        )
                    )
                    firestoreRepository.markEpisodeWatched(item.id.toString(), seasonNumber, episodeNumber)"""

content = content.replace("""                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = false,
                            watchedEpisodes = listOf(episodeKey),
                            addedAt = System.currentTimeMillis()
                        )
                    )""", replacement2)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)
