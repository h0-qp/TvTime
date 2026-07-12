package com.example.ui.screens.details

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    repository: MediaRepository,
    firestoreRepository: com.example.data.firebase.FirestoreRepository,
    mediaId: Int,
    mediaType: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DetailsViewModel = viewModel(
        factory = DetailsViewModelFactory(repository, firestoreRepository, mediaId, mediaType)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TrueBlack
                )
            )
        },
        containerColor = TrueBlack
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is DetailsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GoldYellow
                    )
                }
                is DetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is DetailsUiState.Success -> {
                    val item = state.mediaItem
                    val title = item.name ?: item.title ?: "Unknown"
                    val date = item.first_air_date?.take(4) ?: item.release_date?.take(4) ?: ""
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Backdrop
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w780${item.backdrop_path ?: item.poster_path}",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(DarkGrey)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = title,
                                color = TextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${if (mediaType == "tv") "موسم/مواسم" else "فيلم"} • HBO",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            var selectedTab by remember { mutableIntStateOf(0) }
                            val tabs = if (mediaType == "tv") listOf("حول", "الحلقات") else listOf("حول")

                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = TrueBlack,
                                contentColor = GoldYellow,
                                indicator = { tabPositions ->
                                    TabRowDefaults.Indicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = GoldYellow
                                    )
                                }
                            ) {
                                tabs.forEachIndexed { index, tabTitle ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = { Text(text = tabTitle, fontWeight = FontWeight.Bold, color = if (selectedTab == index) TextPrimary else TextSecondary) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            if (selectedTab == 0) {
                                // About Tab
                                AboutTabContent(item = item)
                            } else {
                                // Episodes Tab
                                if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(item.seasons) { season ->
                                            if (season.season_number > 0) {
                                                val isSelected = state.selectedSeasonNumber == season.season_number
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) GoldYellow else DarkGrey)
                                                        .clickable { viewModel.selectSeason(season.season_number) }
                                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = season.name,
                                                        color = if (isSelected) TrueBlack else TextPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    if (state.isLoadingSeason) {
                                        CircularProgressIndicator(color = GoldYellow, modifier = Modifier.align(Alignment.CenterHorizontally))
                                    } else if (state.selectedSeasonDetails != null) {
                                        state.selectedSeasonDetails.episodes.forEach { episode ->
                                            val episodeKey = "S${episode.season_number}E${episode.episode_number}"
                                            val isWatched = state.firestoreItem?.watchedEpisodes?.contains(episodeKey) == true
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkGrey)
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isWatched) GoldYellow else Color.Transparent)
                                                        .border(2.dp, if (isWatched) GoldYellow else TextSecondary, RoundedCornerShape(8.dp))
                                                        .clickable { 
                                                            if (state.isInWatchlist) {
                                                                viewModel.toggleEpisode(episode.season_number, episode.episode_number)
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isWatched) {
                                                        Icon(Icons.Default.Check, contentDescription = "Watched", tint = TrueBlack, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${episode.episode_number}. ${episode.name}",
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = episode.overview.orEmpty().take(100).let { if (it.length == 100) "$it..." else it }.ifEmpty { "لا توجد قصة للحلقة." },
                                                        color = TextSecondary,
                                                        fontSize = 12.sp,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
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
fun AboutTabContent(item: MediaItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // "أين تُشاهد" section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "أين تُشاهد", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(text = "OSN+", color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = DarkGrey)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "ما أكثر ما يثير اهتمامك في هذا البرنامج؟", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))
        
        val options = listOf("ممثلو الفيلم", "الفكرة الرئيسة", "صانعي العمل", "الشبكة/المنصة", "السلسلة أو العالم", "أخرى")
        options.forEach { option ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkGrey)
                    .clickable { }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option, color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = DarkGrey)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "مشهور", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "أضاف 2M هذا البرنامج", color = TextSecondary, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier.size(48.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF81C784)),
                contentAlignment = Alignment.Center
            ) {
                Text("👥", fontSize = 24.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = DarkGrey)
        Spacer(modifier = Modifier.height(24.dp))
        
        // عرض المعلومات
        Text(text = "عرض المعلومات", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        val year = item.first_air_date?.take(4) ?: item.release_date?.take(4) ?: ""
        // TMDB genres are stored in item.genres?.name if they are returned, but currently TmdbApi returns full details on details request.
        val genres = "خيال, دراما, مغامرة, حركة, حربي" // Using a static one for now as TMDB genre list would require extra parsing
        Text(text = "$year - الحاضر • $genres", color = TextSecondary, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(12.dp))
        val rating = item.vote_average?.let { String.format("%.1f", it) } ?: "0.0"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$rating/10", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            repeat(5) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.background(GoldYellow, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                Text(text = "T", color = TrueBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = item.overview.orEmpty().ifEmpty { "لا توجد قصة متاحة." }, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val trailer = item.videos?.results?.firstOrNull { it.type == "Trailer" }
        if (trailer != null) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DarkGrey).clickable { },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "شاهد المقطع الدعائي", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "00:00", color = TextSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.size(80.dp).background(Color(0xFF333333)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Divider(color = DarkGrey)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Cast
        if (!item.credits?.cast.isNullOrEmpty()) {
            Text(text = "طاقم الممثلين", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(item.credits!!.cast.take(15)) { actor ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                        AsyncImage(
                            model = actor.profile_path?.let { "https://image.tmdb.org/t/p/w185$it" },
                            contentDescription = actor.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = actor.name, color = TextPrimary, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                        Text(text = actor.character, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = DarkGrey)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Similar
        if (!item.similar?.results.isNullOrEmpty()) {
            Text(text = "ما شاهده الناس أيضًا", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(item.similar!!.results.take(10)) { similarItem ->
                    AsyncImage(
                        model = similarItem.poster_path?.let { "https://image.tmdb.org/t/p/w342$it" },
                        contentDescription = similarItem.name ?: similarItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
