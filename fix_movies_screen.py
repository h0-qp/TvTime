with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find("        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {")
end_idx = content.find("@Composable\nfun SectionHeader(title: String) {")

if start_idx != -1 and end_idx != -1:
    new_box = """        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is MoviesUiState.Loading -> {
                    CircularProgressIndicator(color = GoldYellow)
                }
                is MoviesUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is MoviesUiState.Success -> {
                    if (selectedTab == 1) {
                        if (state.watchlist.isEmpty()) {
                            Text("قائمتك فارغة. ابحث عن أفلام لإضافتها!", color = TextSecondary)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                item {
                                    SectionHeader("أفلامك")
                                }
                                
                                items(state.watchlist) { movie ->
                                    MovieWatchlistCard(movie, onNavigateToDetails)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                SectionHeader("أفلام مرتقبة")
                            }
                            
                            items(state.trendingMovies) { movie ->
                                TrendingMovieCard(movie, onNavigateToDetails)
                            }
                        }
                    }
                }
            }
        }
    }
}

"""
    content = content[:start_idx] + new_box + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Failed to find boundaries")
