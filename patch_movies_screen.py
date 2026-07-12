import re

with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'r') as f:
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
    var selectedTab by remember { mutableStateOf(1) }

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
'''                is MoviesUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            SectionHeader("أفلام مقترحة لك")
                        }
                        
                        items(state.trendingMovies) { movie ->
                            MovieWatchlistCard(movie, onNavigateToDetails)
                        }
                    }
                }''',
'''                is MoviesUiState.Success -> {
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
                                SectionHeader("أفلام رائجة")
                            }
                            
                            items(state.trendingMovies) { movie ->
                                TrendingMovieCard(movie, onNavigateToDetails)
                            }
                        }
                    }
                }'''
)

content = content.replace(
'''@Composable
fun MovieWatchlistCard(movie: MediaItem, onNavigateToDetails: (String, Int) -> Unit) {''',
'''@Composable
fun TrendingMovieCard(movie: MediaItem, onNavigateToDetails: (String, Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("movie", movie.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title ?: movie.name ?: "Unknown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.release_date?.take(4) ?: "",
                color = GoldYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.overview.ifEmpty { "لا يوجد وصف متاح..." },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
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
fun MovieWatchlistCard(movie: FirestoreMediaItem, onNavigateToDetails: (String, Int) -> Unit) {'''
)

content = content.replace(
'''        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title ?: movie.name ?: "Unknown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.release_date?.take(4) ?: "",
                color = GoldYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.overview.ifEmpty { "لا يوجد وصف متاح..." },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",''',
'''        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (movie.isWatched) "تمت المشاهدة" else "غير مشاهد",
                color = if (movie.isWatched) GoldYellow else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",'''
)

with open('app/src/main/java/com/example/ui/screens/movies/MoviesScreen.kt', 'w') as f:
    f.write(content)

