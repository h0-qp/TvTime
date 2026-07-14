import re

with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'r') as f:
    content = f.read()

tabs_replacement = """        // Top Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 0 }) {
                Text("المرتقبة", color = if (selectedTab == 0) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 0) {
                    Box(modifier = Modifier.height(3.dp).width(50.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 1 }) {
                Text("قائمة المشاهدة", color = if (selectedTab == 1) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 1) {
                    Box(modifier = Modifier.height(3.dp).width(50.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 2 }) {
                Text("سجل المشاهدة", color = if (selectedTab == 2) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 2) {
                    Box(modifier = Modifier.height(3.dp).width(50.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }"""

content = re.sub(r'        // Top Tabs\n        Row\([^)]*\)\s*\{[^{}]*\{[^{}]*\}[^{}]*\{[^{}]*\}[^{}]*\}', tabs_replacement, content, flags=re.DOTALL)

# Now update the Success block
success_block = """                is MoviesUiState.Success -> {
                    if (selectedTab == 1) {
                        val unwatchedMovies = state.watchlist.filter { !it.isWatched }
                        if (unwatchedMovies.isEmpty()) {
                            Text("قائمتك فارغة. ابحث عن أفلام لإضافتها!", color = TextSecondary)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                item {
                                    SectionHeader("أفلامك")
                                }
                                
                                items(unwatchedMovies) { movie ->
                                    MovieWatchlistCard(movie, onNavigateToDetails)
                                }
                            }
                        }
                    } else if (selectedTab == 2) {
                        val watchedMovies = state.watchlist.filter { it.isWatched }
                        if (watchedMovies.isEmpty()) {
                            Text("لم تشاهد أي أفلام بعد.", color = TextSecondary)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                item {
                                    SectionHeader("سجل المشاهدة")
                                }
                                
                                items(watchedMovies) { movie ->
                                    MovieWatchlistCard(movie, onNavigateToDetails)
                                }
                            }
                        }
                    } else {
                        LazyColumn("""

content = content.replace("""                is MoviesUiState.Success -> {
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
                        LazyColumn(""", success_block)

with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'w') as f:
    f.write(content)
