import sys
content = open("app/src/main/java/com/example/MainActivity.kt").read()

import_statement = "import com.example.ui.screens.person.PersonScreen\n"
if "PersonScreen" not in content:
    content = content.replace("import com.example.ui.screens.details.DetailsScreen\n", "import com.example.ui.screens.details.DetailsScreen\n" + import_statement)

composable_str = """            composable(
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
            }"""

if "route = \"person/{personId}\"" not in content:
    content = content.replace("""            composable(
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
                    onNavigateToDetails = { type, id -> navController.navigate("details/$type/$id") }
                )
            }""", composable_str)

open("app/src/main/java/com/example/MainActivity.kt", "w").write(content)
