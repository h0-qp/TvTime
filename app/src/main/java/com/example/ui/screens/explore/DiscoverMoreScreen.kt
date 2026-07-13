package com.example.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.repository.MediaRepository
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack

@Composable
fun DiscoverMoreScreen(
    firestoreRepository: com.example.data.firebase.FirestoreRepository,
    repository: MediaRepository,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit
) {
    val viewModel: DiscoverMoreViewModel = viewModel(
        factory = DiscoverMoreViewModelFactory(firestoreRepository, repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableStateOf("مسلسلات") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "استكشف المزيد",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("مسلسلات", "أفلام").forEach { tab ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (selectedTab == tab) GoldYellow else TextSecondary,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedTab == tab) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(GoldYellow)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Transparent)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldYellow)
            }
        } else {
            val items = if (selectedTab == "مسلسلات") uiState.tvShows else uiState.movies
            val isLoadingMore = if (selectedTab == "مسلسلات") uiState.isLoadingMoreTv else uiState.isLoadingMoreMovies
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(items) { index, item ->
                    if (index == items.size - 1 && !isLoadingMore) {
                        if (selectedTab == "مسلسلات") viewModel.loadMoreTv() else viewModel.loadMoreMovies()
                    }
                    
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${item.poster_path}",
                        contentDescription = item.title ?: item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(2f/3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkGrey)
                            .clickable {
                                val type = if (selectedTab == "مسلسلات") "tv" else "movie"
                                onNavigateToDetails(type, item.id)
                            }
                    )
                }
                
                if (isLoadingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldYellow, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
