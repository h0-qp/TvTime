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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountCircle
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
    firestoreRepository: com.example.data.firebase.FirestoreRepository,
    repository: MediaRepository,
    onNavigateToDetails: (String, Int) -> Unit,
    onNavigateToDiscoverMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(firestoreRepository, repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var selectedTab by remember { mutableStateOf("تغذية") }
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
                    "اكتشف" -> DiscoverTabContent(uiState, viewModel, onNavigateToDetails, onNavigateToDiscoverMore)
                    "تغذية" -> FeedTabContent(uiState, viewModel, onNavigateToDetails)
                    "مجموعات" -> CollectionsTabContent(uiState)
                    "نشاط" -> ActivityTabContent(uiState, viewModel, onNavigateToDetails)
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
fun DiscoverTabContent(uiState: ExploreUiState, viewModel: ExploreViewModel, onNavigateToDetails: (String, Int) -> Unit, onNavigateToDiscoverMore: () -> Unit) {
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
                DiscoverSection(title = "أفضل البرامج لك", items = uiState.upcomingTvShows, currentState = uiState, viewModel = viewModel, onNavigateToDetails = onNavigateToDetails)
                Spacer(modifier = Modifier.height(24.dp))
                DiscoverSection(title = "البرامج الرائجة", items = uiState.trendingTvShows, currentState = uiState, viewModel = viewModel, onNavigateToDetails = onNavigateToDetails)
                
                Spacer(modifier = Modifier.height(16.dp))
                BrowseAllButton("تصفح جميع البرامج", onClick = onNavigateToDiscoverMore)
                
                Spacer(modifier = Modifier.height(32.dp))
                DiscoverSection(title = "الأفلام الرائجة", items = uiState.trendingMovies, currentState = uiState, viewModel = viewModel, onNavigateToDetails = onNavigateToDetails)
                
                Spacer(modifier = Modifier.height(16.dp))
                BrowseAllButton("تصفح جميع الأفلام", onClick = onNavigateToDiscoverMore)
            }
        }
        else -> {}
    }
}

@Composable
fun DiscoverSection(title: String, items: List<MediaItem>, currentState: ExploreUiState.Success, viewModel: ExploreViewModel, onNavigateToDetails: (String, Int) -> Unit) {
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
                DiscoverPosterCard(item, currentState, { viewModel.toggleWatchlist(it) }, onNavigateToDetails)
            }
        }
    }
}

@Composable
fun DiscoverPosterCard(item: MediaItem, currentState: ExploreUiState.Success, onToggleAdd: (MediaItem) -> Unit, onNavigateToDetails: (String, Int) -> Unit) {
    val isAdded = currentState.watchlistIds.contains(item.id)

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
        
        // Add/Check icon top right
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(if (isAdded) GoldYellow else TrueBlack.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .border(1.dp, GoldYellow, RoundedCornerShape(4.dp))
                .clickable { onToggleAdd(item) },
            contentAlignment = Alignment.Center
        ) {
            if (isAdded) {
                Icon(Icons.Default.Check, contentDescription = "Added", tint = TrueBlack, modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldYellow, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BrowseAllButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(GoldYellow, RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
fun FeedTabContent(uiState: ExploreUiState, viewModel: ExploreViewModel, onNavigateToDetails: (String, Int) -> Unit) {
    if (uiState is ExploreUiState.Success) {
        val feedItems = uiState.feedItems
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(feedItems.size) { index ->
                val item = feedItems[index]
                if (index == feedItems.size - 1 && !uiState.isLoadingMoreFeed) {
                    viewModel.loadMoreFeed()
                }
                
                FeedCard(item, uiState, { viewModel.toggleWatchlist(it) }, onNavigateToDetails)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (uiState.isLoadingMoreFeed) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldYellow)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedCard(item: MediaItem, currentState: ExploreUiState.Success, onToggleAdd: (MediaItem) -> Unit, onNavigateToDetails: (String, Int) -> Unit) {
    val isAdded = currentState.watchlistIds.contains(item.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { onNavigateToDetails(item.media_type ?: "tv", item.id) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w780${item.backdrop_path ?: item.poster_path}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(DarkGrey)
            )
            
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, TrueBlack.copy(alpha = 0.8f))))
            )
            
            // Add/Check icon top right
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .background(if (isAdded) GoldYellow else TrueBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, GoldYellow, RoundedCornerShape(8.dp))
                    .clickable { onToggleAdd(item) },
                contentAlignment = Alignment.Center
            ) {
                if (isAdded) {
                    Icon(Icons.Default.Check, contentDescription = "Added", tint = TrueBlack)
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldYellow)
                }
            }
            
            if (item.media_type == "movie") {
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
                Text(text = item.title ?: item.name ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                val type = if (item.media_type == "movie") "فيلم" else "مسلسل"
                val date = (item.release_date ?: item.first_air_date ?: "").take(4)
                Text(text = "$type • $date", color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        Text(
            text = item.overview ?: "",
            color = TextPrimary,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Right
        )
    }
}

@Composable
fun CollectionsTabContent(uiState: ExploreUiState) {
    if (uiState is ExploreUiState.Success) {
        val genres = uiState.genres
        
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
                items(genres) { item ->
                    CollectionCard(item)
                }
            }
        }
    }
}

@Composable
fun CollectionCard(genre: com.example.data.remote.Genre) {
    // Generate a pseudo-random follower and comment count based on genre id
    val followers = "${(genre.id % 50) + 10}.${genre.id % 9} ألف"
    val comments = "${(genre.id % 5) + 1}.${genre.id % 9} ألف"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF151515))
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image
        Box(
            modifier = Modifier.width(80.dp).fillMaxHeight().background(DarkGrey),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Tv, contentDescription = null, tint = TextSecondary)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Details
        Column(
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = genre.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = followers, color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Add, contentDescription = "Followers", tint = TextSecondary, modifier = Modifier.size(14.dp)) // Group icon placeholder
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = comments, color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowBack, contentDescription = "Comments", tint = TextSecondary, modifier = Modifier.size(14.dp)) // Comment icon placeholder
            }
        }
    }
}

@Composable
fun ActivityTabContent(uiState: ExploreUiState, viewModel: ExploreViewModel, onNavigateToDetails: (String, Int) -> Unit) {
    if (uiState is ExploreUiState.Success) {
        val activityItems = uiState.activityItems
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            activityItems.forEach { item ->
                ActivityCard(item, uiState, { viewModel.toggleWatchlist(it) }, onNavigateToDetails)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActivityCard(item: MediaItem, currentState: ExploreUiState.Success, onToggleAdd: (MediaItem) -> Unit, onNavigateToDetails: (String, Int) -> Unit) {
    val isAdded = currentState.watchlistIds.contains(item.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { onNavigateToDetails(item.media_type ?: "tv", item.id) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w780${item.backdrop_path ?: item.poster_path}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(DarkGrey)
            )
            
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, TrueBlack.copy(alpha = 0.8f))))
            )
            
            // Add/Check icon top right
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .background(if (isAdded) GoldYellow else TrueBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, GoldYellow, RoundedCornerShape(8.dp))
                    .clickable { onToggleAdd(item) },
                contentAlignment = Alignment.Center
            ) {
                if (isAdded) {
                    Icon(Icons.Default.Check, contentDescription = "Added", tint = TrueBlack)
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldYellow)
                }
            }
            
            if (item.media_type == "movie") {
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
                Text(text = item.title ?: item.name ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                val type = if (item.media_type == "movie") "فيلم" else "مسلسل"
                val date = (item.release_date ?: item.first_air_date ?: "").take(4)
                Text(text = "$type • $date", color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        // Watched By section
        val watchedCount = ((item.vote_average ?: 0.0) * 100).toInt() + 100
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
                Text(text = "+$watchedCount ألف", color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.size(40.dp).background(TrueBlack, CircleShape)) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TextSecondary, modifier = Modifier.align(Alignment.Center))
            }
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

