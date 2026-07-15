file_path = "app/src/main/java/com/example/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

import1 = """import com.example.ui.screens.profile.AllTvShowsViewModel
import com.example.ui.screens.profile.AllMoviesViewModel
import com.example.ui.screens.profile.AllTvShowsScreen
import com.example.ui.screens.profile.AllMoviesScreen
"""

if "AllTvShowsViewModel" not in content:
    content = content.replace("import com.example.ui.screens.movies.MoviesScreen", "import com.example.ui.screens.movies.MoviesScreen\n" + import1)

old_profile_composable = """            composable(Screen.Profile.route) { 
                com.example.ui.screens.profile.ProfileScreen(
                    authRepository = appContainer.authRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    onNavigateToDetails = { mediaType, mediaId -> navController.navigate("details/$mediaType/$mediaId") },
                    onSignOut = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }"""

new_profile_composable = """            composable(Screen.Profile.route) { 
                com.example.ui.screens.profile.ProfileScreen(
                    authRepository = appContainer.authRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    onNavigateToDetails = { mediaType, mediaId -> navController.navigate("details/$mediaType/$mediaId") },
                    onNavigateToAllTvShows = { navController.navigate("all_tv_shows") },
                    onNavigateToAllMovies = { navController.navigate("all_movies") },
                    onSignOut = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }
            
            composable("all_tv_shows") {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AllTvShowsViewModel> {
                    AllTvShowsViewModel(appContainer.firestoreRepository, appContainer.mediaRepository)
                }
                AllTvShowsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onShowClick = { id -> navController.navigate("details/tv/$id") }
                )
            }
            
            composable("all_movies") {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AllMoviesViewModel> {
                    AllMoviesViewModel(appContainer.firestoreRepository)
                }
                AllMoviesScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMovieClick = { id -> navController.navigate("details/movie/$id") }
                )
            }"""

if "all_tv_shows" not in content:
    content = content.replace(old_profile_composable, new_profile_composable)

with open(file_path, "w") as f:
    f.write(content)
