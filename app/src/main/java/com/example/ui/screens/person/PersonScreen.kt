package com.example.ui.screens.person

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    repository: MediaRepository,
    personId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PersonViewModel = viewModel(
        factory = PersonViewModelFactory(repository, personId)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(8.dp).background(DarkGrey.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        containerColor = TrueBlack
    ) { innerPadding ->
        when (val state = uiState) {
            is PersonUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = com.example.ui.theme.GoldYellow)
                }
            }
            is PersonUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TextPrimary)
                }
            }
            is PersonUiState.Success -> {
                val person = state.person
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        AsyncImage(
                            model = person.profile_path?.let { "https://image.tmdb.org/t/p/w500$it" },
                            contentDescription = person.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .background(DarkGrey)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = person.name,
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            val subtitle = buildString {
                                if (!person.birthday.isNullOrEmpty()) append(person.birthday)
                                if (person.gender != null) {
                                    if (isNotEmpty()) append(" · ")
                                    append(if (person.gender == 1) "أنثى" else if (person.gender == 2) "ذكر" else "غير محدد")
                                }
                            }
                            if (subtitle.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subtitle,
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (!person.biography.isNullOrEmpty()) {
                        item {
                            Text(
                                text = person.biography,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Description above from the Wikipedia article ${person.name}, licensed under CC-BY-SA, full list of contributors on Wikipedia.",
                                color = TextSecondary.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    person.combined_credits?.cast?.let { castList ->
                        val movies = castList.filter { it.media_type == "movie" }
                        val shows = castList.filter { it.media_type == "tv" }

                        if (movies.isNotEmpty()) {
                            item {
                                Text(
                                    text = "الأفلام",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(movies) { movie ->
                                        MediaItemCard(
                                            item = movie,
                                            onClick = { onNavigateToDetails("movie", movie.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        if (shows.isNotEmpty()) {
                            item {
                                Text(
                                    text = "المسلسلات",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(shows) { show ->
                                        MediaItemCard(
                                            item = show,
                                            onClick = { onNavigateToDetails("tv", show.id) }
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
private fun MediaItemCard(item: MediaItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.poster_path?.let { "https://image.tmdb.org/t/p/w342$it" },
            contentDescription = item.title ?: item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkGrey)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title ?: item.name ?: "",
            color = TextPrimary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val date = item.release_date ?: item.first_air_date
        if (!date.isNullOrEmpty()) {
            Text(
                text = date.take(4),
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
