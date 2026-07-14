with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find("        // Top Tabs")
end_idx = content.find("        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {")

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
        }
"""

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + tabs_replacement + content[end_idx:]
    print("Tabs replaced")
else:
    print("Could not find tabs")

# Now the success block
success_start = content.find("                is MoviesUiState.Success -> {")
if success_start != -1:
    success_end = content.find("                    } else {", success_start)
    if success_end != -1:
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
"""
        content = content[:success_start] + success_block + content[success_end:]
        print("Success block replaced")
    else:
        print("Could not find success end")
else:
    print("Could not find success start")

with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'w') as f:
    f.write(content)
