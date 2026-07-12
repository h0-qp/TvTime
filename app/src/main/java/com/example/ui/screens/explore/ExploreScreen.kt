package com.example.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Search Bar
        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("بحث عن مسلسلات أو أفلام...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldYellow,
                    unfocusedBorderColor = DarkGrey,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkGrey,
                    unfocusedContainerColor = DarkGrey
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton("تغذية", true)
            TabButton("اكتشف", false)
            TabButton("مجموعات", false)
            TabButton("نشاط", false)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is ExploreUiState.Idle -> {
                    Text("ابحث عن أي مسلسل أو فيلم للبدء", color = TextSecondary, fontSize = 16.sp)
                }
                is ExploreUiState.Loading -> {
                    CircularProgressIndicator(color = GoldYellow)
                }
                is ExploreUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ExploreUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Text("لم يتم العثور على نتائج", color = TextSecondary, fontSize = 16.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.results) { item ->
                                SearchResultCard(item, onNavigateToDetails)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(title: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) GoldYellow else DarkGrey,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 18.dp, vertical = 8.dp)
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
