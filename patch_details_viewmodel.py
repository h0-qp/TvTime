import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

# Replace addOrUpdateMedia
add_media_target = """                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = false,
                            addedAt = System.currentTimeMillis()
                        )
                    )"""

add_media_replacement = """                    firestoreRepository.addOrUpdateMedia(
                        FirestoreMediaItem(
                            id = item.id,
                            title = item.name ?: item.title ?: "Unknown",
                            posterPath = item.poster_path,
                            mediaType = mediaType,
                            isWatched = false,
                            addedAt = System.currentTimeMillis()
                        )
                    )
                    if (mediaType == "tv") {
                        firestoreRepository.addTvShowToWatchlist(item.id.toString())
                    }"""

remove_media_target = """                    firestoreRepository.removeMedia(item.id)"""

remove_media_replacement = """                    firestoreRepository.removeMedia(item.id)
                    if (mediaType == "tv") {
                        firestoreRepository.removeTvShowFromWatchlist(item.id.toString())
                    }"""

content = content.replace(add_media_target, add_media_replacement)
content = content.replace(remove_media_target, remove_media_replacement)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)

