package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.tvshows.TvShowsScreen
import com.example.ui.screens.movies.MoviesScreen
import com.example.ui.screens.profile.AllTvShowsViewModel
import com.example.ui.screens.profile.AllMoviesViewModel
import com.example.ui.screens.profile.AllTvShowsScreen
import com.example.ui.screens.profile.AllMoviesScreen

import com.example.ui.theme.TrackVerseTheme
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object TvShows : Screen("tv_shows", "مسلسلات", Icons.Outlined.Tv)
    object Movies : Screen("movies", "أفلام", Icons.Outlined.Movie)
    object Explore : Screen("explore", "استكشف", Icons.Outlined.Search)
    object Profile : Screen("profile", "الملف الشخصي", Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    Screen.TvShows,
    Screen.Movies,
    Screen.Explore,
    Screen.Profile
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackVerseTheme {
                val appContainer = (application as TrackVerseApplication).container
                TrackVerseApp(appContainer)
            }
        }
    }
}

@Composable
fun TrackVerseApp(appContainer: com.example.data.AppContainer) {
    val navController = rememberNavController()
    val isUserLoggedIn = appContainer.authRepository.currentUser != null
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = androidx.compose.runtime.remember {
        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    }
    var onboardingCompleted by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(sharedPrefs.getBoolean("onboarding_completed", false))
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val startDestination = if (!onboardingCompleted) {
        "onboarding"
    } else if (isUserLoggedIn) {
        Screen.TvShows.route
    } else {
        "auth"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.example.ui.theme.TrueBlack
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                com.example.ui.screens.onboarding.OnboardingScreen(
                    onComplete = {
                        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
                        onboardingCompleted = true
                        val nextRoute = if (isUserLoggedIn) Screen.TvShows.route else "auth"
                        navController.navigate(nextRoute) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("auth") {
                com.example.ui.screens.auth.AuthScreen(
                    authRepository = appContainer.authRepository,
                    onAuthSuccess = {
                        navController.navigate(Screen.TvShows.route) {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.TvShows.route) { 
                val tvViewModel: com.example.ui.screens.tvshows.TvShowsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.ui.screens.tvshows.TvShowsViewModelFactory(appContainer.mediaRepository, appContainer.firestoreRepository)
                )
                TvShowsScreen(
                    viewModel = tvViewModel,
                    onNavigateToDetails = { mediaType, mediaId ->
                        navController.navigate("details/$mediaType/$mediaId")
                    }
                ) 
            }
            composable(Screen.Movies.route) { 
                MoviesScreen(
                    repository = appContainer.mediaRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    onNavigateToDetails = { mediaType, mediaId ->
                        navController.navigate("details/$mediaType/$mediaId")
                    }
                ) 
            }
            composable(Screen.Explore.route) { 
                com.example.ui.screens.explore.ExploreScreen(
                    repository = appContainer.mediaRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    onNavigateToDetails = { mediaType, mediaId ->
                        navController.navigate("details/$mediaType/$mediaId")
                    },
                    onNavigateToDiscoverMore = {
                        navController.navigate("discover_more")
                    }
                ) 
            }
            
            composable("discover_more") {
                com.example.ui.screens.explore.DiscoverMoreScreen(
                    repository = appContainer.mediaRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { mediaType, mediaId ->
                        navController.navigate("details/$mediaType/$mediaId")
                    }
                )
            }
            composable(Screen.Profile.route) { 
                com.example.ui.screens.profile.ProfileScreen(
                    authRepository = appContainer.authRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    mediaRepository = appContainer.mediaRepository,
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
            }
            composable(
                route = "details/{mediaType}/{mediaId}",
                arguments = listOf(
                    androidx.navigation.navArgument("mediaType") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("mediaId") { type = androidx.navigation.NavType.IntType }
                )
            ) { backStackEntry ->
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "tv"
                val mediaId = backStackEntry.arguments?.getInt("mediaId") ?: 0
                com.example.ui.screens.details.DetailsScreen(
                    repository = appContainer.mediaRepository,
                    firestoreRepository = appContainer.firestoreRepository,
                    mediaId = mediaId,
                    mediaType = mediaType,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { type, id -> navController.navigate("details/$type/$id") },
                    onNavigateToPerson = { id -> navController.navigate("person/$id") }
                )
            }
            
            composable(
                route = "person/{personId}",
                arguments = listOf(
                    androidx.navigation.navArgument("personId") { type = androidx.navigation.NavType.IntType }
                )
            ) { backStackEntry ->
                val personId = backStackEntry.arguments?.getInt("personId") ?: 0
                com.example.ui.screens.person.PersonScreen(
                    repository = appContainer.mediaRepository,
                    personId = personId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { type, id -> navController.navigate("details/$type/$id") }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = title, color = TextPrimary)
    }
}
