import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                TvShowsScreen(
                    repository = appContainer.mediaRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    onNavigateToDetails = { mediaType, mediaId ->"""

replacement = """                val tvViewModel: com.example.ui.screens.tvshows.TvShowsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.ui.screens.tvshows.TvShowsViewModelFactory(appContainer.mediaRepository, appContainer.firestoreRepository)
                )
                TvShowsScreen(
                    viewModel = tvViewModel,
                    onNavigateToDetails = { mediaType, mediaId ->"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
