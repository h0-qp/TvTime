import re

with open("app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val uiState: StateFlow<TvShowsUiState> = _uiState",
    "val uiState: StateFlow<TvShowsUiState> = _uiState\n\n    private val detailsCache = mutableMapOf<String, com.example.data.remote.MediaItem>()\n    private val seasonCache = mutableMapOf<String, List<com.example.data.remote.Episode>>()"
)

with open("app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt", "w") as f:
    f.write(content)
