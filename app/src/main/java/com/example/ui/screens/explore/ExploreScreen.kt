package com.example.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack

@Composable
fun ExploreScreen(
    repository: MediaRepository,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var selectedTab by remember { mutableStateOf("اكتشف") }
    val tabs = listOf("تغذية", "اكتشف", "مجموعات", "نشاط")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Search Bar
        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("بحث", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = TrueBlack,
                    unfocusedContainerColor = TrueBlack
                )
            )
        }
        
        Divider(color = DarkGrey, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                TabButton(
                    title = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            if (searchQuery.isNotBlank()) {
                SearchResultsContent(uiState, onNavigateToDetails)
            } else {
                when (selectedTab) {
                    "اكتشف" -> DiscoverTabContent(uiState, onNavigateToDetails)
                    "تغذية" -> FeedTabContent()
                    "مجموعات" -> CollectionsTabContent()
                    "نشاط" -> ActivityTabContent()
                }
            }
        }
    }
}

@Composable
fun SearchResultsContent(uiState: ExploreUiState, onNavigateToDetails: (String, Int) -> Unit) {
    when (uiState) {
        is ExploreUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldYellow)
            }
        }
        is ExploreUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is ExploreUiState.Success -> {
            if (uiState.results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لم يتم العثور على نتائج", color = TextSecondary, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.results) { item ->
                        SearchResultCard(item, onNavigateToDetails)
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun TabButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) GoldYellow else DarkGrey,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            title, 
            color = if (isSelected) TrueBlack else TextPrimary, 
            fontWeight = FontWeight.Bold, 
            fontSize = 14.sp
        )
    }
}

@Composable
fun DiscoverTabContent(uiState: ExploreUiState, onNavigateToDetails: (String, Int) -> Unit) {
    when (uiState) {
        is ExploreUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldYellow)
            }
        }
        is ExploreUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                DiscoverSection(title = "أفضل البرامج لك", items = uiState.upcomingTvShows, onNavigateToDetails)
                Spacer(modifier = Modifier.height(24.dp))
                DiscoverSection(title = "البرامج الرائجة", items = uiState.trendingTvShows, onNavigateToDetails)
                
                Spacer(modifier = Modifier.height(16.dp))
                BrowseAllButton("تصفح جميع البرامج")
                
                Spacer(modifier = Modifier.height(32.dp))
                DiscoverSection(title = "الأفلام الرائجة", items = uiState.trendingMovies, onNavigateToDetails)
                
                Spacer(modifier = Modifier.height(16.dp))
                BrowseAllButton("تصفح جميع الأفلام")
            }
        }
        else -> {}
    }
}

@Composable
fun DiscoverSection(title: String, items: List<MediaItem>, onNavigateToDetails: (String, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "See All", tint = TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.take(10)) { item ->
                DiscoverPosterCard(item, onNavigateToDetails)
            }
        }
    }
}

@Composable
fun DiscoverPosterCard(item: MediaItem, onNavigateToDetails: (String, Int) -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { 
                val type = if (item.media_type == "tv") "tv" else "movie"
                onNavigateToDetails(type, item.id) 
            }
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w342${item.poster_path}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().background(DarkGrey)
        )
        
        // Plus icon top right
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(TrueBlack.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .border(1.dp, GoldYellow, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldYellow, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun BrowseAllButton(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(GoldYellow, RoundedCornerShape(8.dp))
            .clickable { }
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = TrueBlack)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = text, color = TrueBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(16.dp))
                // Placeholder icon
                Icon(Icons.Default.Tv, contentDescription = null, tint = TrueBlack)
            }
        }
    }
}

@Composable
fun FeedTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        FeedCard(
            title = "Here It All Begins (2020)",
            subtitle = "6 موسم/مواسم • TF1",
            description = "Maxime, Rose and Antoine immerse themselves in the life of a school that will train the future big names in...",
            imageRes = "https://image.tmdb.org/t/p/w780/kI20F9W7EIf9m2IqBntY769m0W2.jpg",
            hasPlayButton = false
        )
        Spacer(modifier = Modifier.height(16.dp))
        FeedCard(
            title = "Crime 101",
            subtitle = "2h 23m • جريمة, دراما",
            description = "When an elusive thief whose high-stakes heists unfold along the iconic 101 freeway in Los Angeles e...",
            imageRes = "https://image.tmdb.org/t/p/w780/jYl0U8jEENMEslEXcmpCG4FzD8t.jpg", // Mock image path for Chris Hemsworth
            hasPlayButton = true
        )
    }
}

@Composable
fun FeedCard(title: String, subtitle: String, description: String, imageRes: String, hasPlayButton: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = imageRes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(DarkGrey)
            )
            
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, TrueBlack.copy(alpha = 0.8f))))
            )
            
            // Plus icon top right
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .background(TrueBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, GoldYellow, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldYellow)
            }
            
            if (hasPlayButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .background(TrueBlack.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        Text(
            text = description,
            color = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Right
        )
    }
}

@Composable
fun CollectionsTabContent() {
    data class CollectionItem(val title: String, val followers: String, val comments: String, val imageRes: String)
    val collections = listOf(
        CollectionItem("Anime", "55.5 ألف", "2.87 ألف", "https://image.tmdb.org/t/p/w300/yZdYKtcxbsF3gX6NtcBfW8M8zIe.jpg"),
        CollectionItem("K-Drama", "47.8 ألف", "2.51 ألف", "https://image.tmdb.org/t/p/w300/qcpC9lv6VLL4Zw45EveYELyje1w.jpg"),
        CollectionItem("Horror", "30.6 ألف", "1.14 ألف", "https://image.tmdb.org/t/p/w300/50L3iN0OOSnUKOONrV1Fj2P2665.jpg"),
        CollectionItem("Sitcoms", "25.4 ألف", "858", "https://image.tmdb.org/t/p/w300/f496cm9enuEsZkSPzCwnTESEK5s.jpg"),
        CollectionItem("Harry Potter", "21.2 ألف", "571", "https://image.tmdb.org/t/p/w300/wuMc08IPKEbQ08W32EwN0kFqS7.jpg"),
        CollectionItem("Rom-Com", "12.1 ألف", "373", "https://image.tmdb.org/t/p/w300/i91T5vEEtZ2kZnt1YvYlI0D9uS1.jpg"),
        CollectionItem("Disney", "8.92 ألف", "310", "https://image.tmdb.org/t/p/w300/qX1eZ4G5yA6Ea9QZ6V07NTh5a8v.jpg"),
        CollectionItem("Star Wars", "6.5 ألف", "241", "https://image.tmdb.org/t/p/w300/oEzzA7yX4uV7k0GlaL2JtXpIqZ0.jpg")
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "فرز حسب", color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "مشهور", color = Color(0xFF64B5F6), fontSize = 14.sp)
            }
            Icon(Icons.Default.ArrowBack, contentDescription = "Help", tint = TextSecondary, modifier = Modifier.size(18.dp)) // Placeholder for question mark
        }
        
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(collections) { item ->
                CollectionCard(item)
            }
        }
    }
}

@Composable
fun CollectionCard(item: Any) {
    // Reflection used to bypass type checking since we defined data class locally
    val title = item.javaClass.getMethod("getTitle").invoke(item) as String
    val followers = item.javaClass.getMethod("getFollowers").invoke(item) as String
    val comments = item.javaClass.getMethod("getComments").invoke(item) as String
    val imageRes = item.javaClass.getMethod("getImageRes").invoke(item) as String

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF151515)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Details
        Column(
            modifier = Modifier.weight(1f).padding(end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = comments, color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowBack, contentDescription = "Comments", tint = TextSecondary, modifier = Modifier.size(14.dp)) // Comment icon placeholder
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = followers, color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Add, contentDescription = "Followers", tint = TextSecondary, modifier = Modifier.size(14.dp)) // Group icon placeholder
            }
        }
        
        // Image
        AsyncImage(
            model = imageRes,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(80.dp).fillMaxHeight().background(DarkGrey)
        )
    }
}

@Composable
fun ActivityTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ActivityCard(
            title = "JoJo's Bizarre Adventure (2012)",
            subtitle = "6 موسم/مواسم • Netflix",
            watchedBy = "+622 ألف",
            imageRes = "https://image.tmdb.org/t/p/w780/6xS53F6v4O2P9QyZ4C6N6gO6hM3.jpg",
            hasPlayButton = false
        )
        Spacer(modifier = Modifier.height(16.dp))
        ActivityCard(
            title = "Terminator 2: Judgment Day",
            subtitle = "2h 17m • حركة, Science Fiction, إثارة",
            watchedBy = "+333 ألف",
            imageRes = "https://image.tmdb.org/t/p/w780/xKbVWqO52q0vG1rXq3d2lK8YxXy.jpg",
            hasPlayButton = true
        )
    }
}

@Composable
fun ActivityCard(title: String, subtitle: String, watchedBy: String, imageRes: String, hasPlayButton: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = imageRes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(DarkGrey)
            )
            
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, TrueBlack.copy(alpha = 0.8f))))
            )
            
            // Plus icon top right
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .background(TrueBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, GoldYellow, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldYellow)
            }
            
            if (hasPlayButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .background(TrueBlack.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF231E05))
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "تمت مشاهدته بواسطة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = watchedBy, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.size(40.dp).background(TrueBlack, CircleShape)) // Profile placeholder
        }
    }
}

@Composable
fun SearchResultCard(item: MediaItem, onNavigateToDetails: (String, Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { 
                val type = if (item.media_type == "tv") "tv" else "movie"
                onNavigateToDetails(type, item.id) 
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${item.poster_path}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 64.dp, height = 96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name ?: item.title ?: "Unknown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val typeStr = if (item.media_type == "tv") "مسلسل" else "فيلم"
            val dateStr = item.first_air_date ?: item.release_date ?: ""
            val yearStr = if (dateStr.length >= 4) dateStr.substring(0, 4) else ""
            
            Text(
                text = "$typeStr ${if (yearStr.isNotEmpty()) "• $yearStr" else ""}",
                color = GoldYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.overview.orEmpty().ifEmpty { "لا يوجد وصف متاح..." },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

