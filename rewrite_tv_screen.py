import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'r') as f:
    content = f.read()

replacement = """package com.example.ui.screens.tvshows

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvShowsScreen(
    viewModel: TvShowsViewModel,
    onNavigateToDetails: (String, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(1) } // 0 = Upcoming, 1 = Watchlist

    Column(modifier = Modifier.fillMaxSize().background(TrueBlack)) {
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

        when (uiState) {
            is TvShowsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldYellow)
                }
            }
            is TvShowsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (uiState as TvShowsUiState.Error).message,
                        color = Color.Red
                    )
                }
            }
            is TvShowsUiState.Success -> {
                val successState = uiState as TvShowsUiState.Success
                
                if (selectedTab == 1) {
                    if (successState.watchedHistory.isEmpty() && successState.watchNext.isEmpty() && successState.notWatchedForAWhile.isEmpty() && successState.notStarted.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("قائمتك فارغة. ابحث عن مسلسلات لإضافتها!", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            if (successState.watchedHistory.isNotEmpty()) {
                                item { SectionHeader("سجل المشاهدة") }
                                items(successState.watchedHistory, key = { "watched_${it.showId}_${it.seasonNumber}_${it.episodeNumber}" }) { ep ->
                                    WatchedEpisodeCard(
                                        epData = ep,
                                        onNavigateToDetails = onNavigateToDetails,
                                        onUnwatch = {
                                            viewModel.markEpisodeUnwatched(ep.showId, ep.seasonNumber, ep.episodeNumber)
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }
                            if (successState.watchNext.isNotEmpty()) {
                                item { SectionHeader("شاهد التالي") }
                                items(successState.watchNext, key = { "next_${it.showId}_${it.seasonNumber}_${it.episodeNumber}" }) { ep ->
                                    NextEpisodeCard(
                                        epData = ep,
                                        onNavigateToDetails = onNavigateToDetails,
                                        onWatch = {
                                            viewModel.markEpisodeWatched(ep.showId, ep.seasonNumber, ep.episodeNumber)
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }
                            if (successState.notWatchedForAWhile.isNotEmpty()) {
                                item { SectionHeader("لم يتم مشاهدته منذ فترة") }
                                items(successState.notWatchedForAWhile, key = { "awhile_${it.showId}_${it.seasonNumber}_${it.episodeNumber}" }) { ep ->
                                    NextEpisodeCard(
                                        epData = ep,
                                        onNavigateToDetails = onNavigateToDetails,
                                        onWatch = {
                                            viewModel.markEpisodeWatched(ep.showId, ep.seasonNumber, ep.episodeNumber)
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }
                            if (successState.notStarted.isNotEmpty()) {
                                item { SectionHeader("لم يبدأ") }
                                items(successState.notStarted, key = { "notstarted_${it.showId}" }) { show ->
                                    NotStartedCard(
                                        showData = show,
                                        onNavigateToDetails = onNavigateToDetails,
                                        onStart = {
                                            viewModel.markEpisodeWatched(show.showId, 1, 1)
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (successState.upcomingEpisodes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد حلقات مرتقبة قريباً.", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            item { SectionHeader("حلقات مرتقبة") }
                            items(successState.upcomingEpisodes, key = { "upcoming_${it.show.id}_${it.episodeToAir.id}" }) { ep ->
                                UpcomingEpisodeCard(
                                    epData = ep,
                                    onNavigateToDetails = onNavigateToDetails,
                                    modifier = Modifier.animateItemPlacement()
                                )
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
fun UpcomingEpisodeCard(
    epData: UpcomingEpisodeData,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", epData.show.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = epData.showDetails.name ?: "Unknown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "S${epData.episodeToAir.season_number} E${epData.episodeToAir.episode_number}: ${epData.episodeToAir.name}",
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            val daysText = when {
                epData.daysDifference < 0 -> "منذ ${-epData.daysDifference} أيام"
                epData.daysDifference == 0L -> "اليوم"
                epData.daysDifference == 1L -> "غداً"
                else -> "بعد ${epData.daysDifference} أيام"
            }
            Text(
                text = daysText,
                color = GoldYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        val imageUrl = epData.showDetails.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
        AsyncImage(
            model = imageUrl,
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
fun WatchedEpisodeCard(
    epData: WatchedEpisodeData, 
    onNavigateToDetails: (String, Int) -> Unit,
    onUnwatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seasonStr = epData.seasonNumber.toString().padStart(2, '0')
    val epStr = epData.episodeNumber.toString().padStart(2, '0')

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", epData.showId.toInt()) }
            .padding(12.dp)
            .alpha(0.5f), // Watched items are faded
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox - Watched State
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF4CAF50), CircleShape)
                .clickable { onUnwatch() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Unwatch", tint = Color.White)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("< ${epData.showName.uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S$seasonStr | E$epStr",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = epData.episodeName,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Image
        val imageUrl = epData.backdropPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            ?: epData.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
        
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }
    }
}

@Composable
fun NextEpisodeCard(
    epData: NextEpisodeData, 
    onNavigateToDetails: (String, Int) -> Unit,
    onWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seasonStr = epData.seasonNumber.toString().padStart(2, '0')
    val epStr = epData.episodeNumber.toString().padStart(2, '0')

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", epData.showId.toInt()) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox - Unwatched State
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
                .clickable { onWatch() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Watch", tint = Color.LightGray)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("< ${epData.showName.uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S$seasonStr | E$epStr",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = epData.episodeName,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Image
        val imageUrl = epData.backdropPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            ?: epData.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun NotStartedCard(
    showData: NotStartedShowData, 
    onNavigateToDetails: (String, Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", showData.showId.toInt()) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = showData.showName,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("تمت المشاهدة: 0 حلقة من ${showData.totalEpisodes}", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ابدأ الحلقة الأولى", color = TrueBlack, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        val imageUrl = showData.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 64.dp, height = 96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )
    }
}
"""

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'w') as f:
    f.write(replacement)
