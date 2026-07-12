package com.example.ui.screens.tvshows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.data.firebase.FirestoreMediaItem
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun TvShowsScreen(
    repository: MediaRepository,
    firestoreRepository: com.example.data.firebase.FirestoreRepository,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: TvShowsViewModel = viewModel(
        factory = TvShowsViewModelFactory(repository, firestoreRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
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
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is TvShowsUiState.Loading -> {
                    CircularProgressIndicator(color = GoldYellow)
                }
                is TvShowsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is TvShowsUiState.Success -> {
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
                            if (state.upcomingEpisodes.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("لا توجد مسلسلات مرتقبة", color = TextSecondary)
                                    }
                                }
                            } else {
                                val grouped = state.upcomingEpisodes.groupBy { 
                                    when (it.daysDifference) {
                                        -1L -> "أمس"
                                        0L -> "اليوم"
                                        1L -> "غدًا"
                                        else -> if (it.daysDifference > 1) "بعد ${it.daysDifference} أيام" else "قبل ${-it.daysDifference} أيام"
                                    }
                                }
                                
                                grouped.forEach { (header, episodes) ->
                                    item {
                                        SectionHeader(header)
                                    }
                                    items(episodes) { epData ->
                                        UpcomingEpisodeCard(
                                            epData = epData, 
                                            onNavigateToDetails = onNavigateToDetails,
                                            onToggleWatched = { showId, epKey -> 
                                                viewModel.toggleEpisodeWatched(showId, epKey) 
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF333333), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
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
                text = show.overview.orEmpty().ifEmpty { "لا يوجد وصف متاح..." },
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
fun WatchlistCard(show: FirestoreMediaItem, onNavigateToDetails: (String, Int) -> Unit) {
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
                text = show.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("تمت المشاهدة: ${show.watchedEpisodes.size} حلقة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${show.posterPath}",
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
fun UpcomingEpisodeCard(
    epData: UpcomingEpisodeData, 
    onNavigateToDetails: (String, Int) -> Unit,
    onToggleWatched: (Int, String) -> Unit
) {
    val seasonStr = epData.episodeToAir.season_number.toString().padStart(2, '0')
    val epStr = epData.episodeToAir.episode_number.toString().padStart(2, '0')
    val epKey = "S${epData.episodeToAir.season_number}E${epData.episodeToAir.episode_number}"
    val isWatched = epData.show.watchedEpisodes.contains(epKey)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(if (isWatched) Color(0xFF1E1E1E) else DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", epData.show.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isWatched) Color(0xFF4CAF50) else Color.White, CircleShape)
                .clickable { onToggleWatched(epData.show.id, epKey) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Watched", tint = if (isWatched) Color.White else Color.LightGray)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            // Show name pill
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("< ${(epData.showDetails.name ?: epData.showDetails.title ?: epData.show.title).uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S$seasonStr | E$epStr",
                color = if (isWatched) Color.Gray else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = epData.episodeToAir.name,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Image
        val imageUrl = epData.episodeToAir.still_path?.let { "https://image.tmdb.org/t/p/w500$it" } 
            ?: epData.showDetails.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
        
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isWatched) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
            }
        }
    }
}
