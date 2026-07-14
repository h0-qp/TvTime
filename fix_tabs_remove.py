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
        }"""

success_block = """                is MoviesUiState.Success -> {
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
                    } else {"""

content = re.sub(r'        // Top Tabs\n        Row\([^)]*\)\s*\{[^{}]*\{[^{}]*\}[^{}]*\{[^{}]*\}[^{}]*\{[^{}]*\}[^{}]*\}', tabs_replacement, content, flags=re.DOTALL)
content = re.sub(r'                is MoviesUiState\.Success -> \{\n                    if \(selectedTab == 1\) \{.*?\n                    \} else \{', success_block, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'w') as f:
    f.write(content)
