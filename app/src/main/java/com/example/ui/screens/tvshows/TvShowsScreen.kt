package com.example.ui.screens.tvshows

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Box(modifier = Modifier.fillMaxSize().background(TrueBlack)) {
        when (uiState) {
            is WatchlistUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GoldYellow
                )
            }
            is WatchlistUiState.Error -> {
                Text(
                    text = (uiState as WatchlistUiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is WatchlistUiState.Success -> {
                val successState = uiState as WatchlistUiState.Success
                
                if (successState.watchedHistory.isEmpty() && successState.watchNext.isEmpty() && successState.notWatchedForAWhile.isEmpty() && successState.notStarted.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("قائمتك فارغة. ابحث عن مسلسلات لإضافتها!", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                    // 1. Watched History
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

                    // 2. Watch Next
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

                    // 3. Not Watched For A While
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

                    // 4. Not Started
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
