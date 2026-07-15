package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.TrueBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTvShowsScreen(
    viewModel: AllTvShowsViewModel,
    onBackClick: () -> Unit,
    onShowClick: (Int) -> Unit
) {
    val tvShows by viewModel.tvShows.collectAsState()
    val tmdbDetails by viewModel.tmdbDetails.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مسلسلات", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrueBlack)
            )
        },
        containerColor = TrueBlack
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(1.dp),
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(tvShows) { show ->
                val details = tmdbDetails[show.id]
                val watchedCount = show.watchedEpisodes.size
                val totalCount = details?.number_of_episodes ?: 0
                val status = details?.status ?: ""
                
                Box(modifier = Modifier
                    .aspectRatio(2f/3f)
                    .clickable { onShowClick(show.id) }
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${show.posterPath}",
                        contentDescription = show.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (watchedCount > 0 && totalCount > 0) {
                        val progress = (watchedCount.toFloat() / totalCount).coerceIn(0f, 1f)
                        
                        val lineColor = if (watchedCount >= totalCount) {
                            if (status.equals("Ended", ignoreCase = true) || status.equals("Canceled", ignoreCase = true)) {
                                Color(0xFF9C27B0) // Purple
                            } else {
                                Color(0xFF4CAF50) // Green
                            }
                        } else {
                            Color(0xFFFFC107) // Yellow
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth(if (watchedCount >= totalCount) 1f else progress)
                                .height(4.dp)
                                .background(lineColor)
                        )
                    }
                }
            }
        }
    }
}
