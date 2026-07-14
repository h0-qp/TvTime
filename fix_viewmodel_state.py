import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'r') as f:
    content = f.read()

replacement = """                    _uiState.value = DetailsUiState.Success(
                        mediaItem = response,
                        isInWatchlist = isInWatchlist,
                        firestoreItem = firestoreItem,
                        selectedSeasonDetails = seasonDetails,
                        selectedSeasonNumber = seasonNumber,
                        selectedEpisodeDetails = if (currentState is DetailsUiState.Success) currentState.selectedEpisodeDetails else null
                    )"""

content = re.sub(r'                    _uiState\.value = DetailsUiState\.Success\(\n                        mediaItem = response,\n                        isInWatchlist = isInWatchlist,\n                        firestoreItem = firestoreItem,\n                        selectedSeasonDetails = seasonDetails,\n                        selectedSeasonNumber = seasonNumber\n                    \)', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/details/DetailsViewModel.kt', 'w') as f:
    f.write(content)
