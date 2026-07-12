import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.runtime.Composable', 'import androidx.compose.runtime.*\nimport com.example.data.firebase.FirestoreMediaItem')

content = content.replace(
'''    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Top Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("المرتقبة", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("قائمة المشاهدة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.height(3.dp).width(50.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
            }
        }''',
'''    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(1) } // 0 = Upcoming, 1 = Watchlist

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Top Tabs
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
        }'''
)

content = content.replace(
'''                is TvShowsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            SectionHeader("لم يتم مشاهدته منذ فترة")
                        }
                        
                        items(state.trendingShows) { show ->
                            WatchlistCard(show, onNavigateToDetails)
                        }
                    }
                }''',
'''                is TvShowsUiState.Success -> {
                    if (selectedTab == 1) {
                        if (state.watchlist.isEmpty()) {
                            Text("قائمتك فارغة. ابحث عن مسلسلات لإضافتها!", color = TextSecondary)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                item {
                                    SectionHeader("مسلسلاتك")
                                }
                                
                                items(state.watchlist) { show ->
                                    WatchlistCard(show, onNavigateToDetails)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                SectionHeader("أفلام ومسلسلات رائجة")
                            }
                            
                            items(state.trendingShows) { show ->
                                TrendingCard(show, onNavigateToDetails)
                            }
                        }
                    }
                }'''
)

content = content.replace(
'''@Composable
fun WatchlistCard(show: MediaItem, onNavigateToDetails: (String, Int) -> Unit) {''',
'''@Composable
fun TrendingCard(show: MediaItem, onNavigateToDetails: (String, Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", show.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = show.name ?: show.title ?: "Unknown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = show.first_air_date?.take(4) ?: "",
                color = GoldYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = show.overview.ifEmpty { "لا يوجد وصف متاح..." },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${show.poster_path}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 64.dp, height = 96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )
    }
}

@Composable
fun WatchlistCard(show: FirestoreMediaItem, onNavigateToDetails: (String, Int) -> Unit) {'''
)

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'w') as f:
    f.write(content)

