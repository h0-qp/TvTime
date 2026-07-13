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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        val state = uiState
                        if (state is DetailsUiState.Success && state.selectedEpisodeDetails != null) {
                            viewModel.selectEpisode(null)
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TrueBlack
                )
            )
        },
        bottomBar = {
            if (uiState is DetailsUiState.Success) {
                val state = uiState as DetailsUiState.Success
                if (state.selectedEpisodeDetails == null) {
                    val backgroundColor = if (state.isInWatchlist) TrueBlack else GoldYellow
                    val textColor = if (state.isInWatchlist) GoldYellow else TrueBlack
                    val icon = if (state.isInWatchlist) Icons.Default.Check else Icons.Default.Add
                    val textStr = if (state.isInWatchlist) "تمت الإضافة" else (if (mediaType == "tv") "إضافة مسلسل" else "إضافة فيلم")
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .clickable { viewModel.toggleWatchlist() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = textColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(textStr, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
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
                    if (state.selectedEpisodeDetails != null) {
                        EpisodeDetailsContent(
                            episode = state.selectedEpisodeDetails,
                            isWatched = state.firestoreItem?.watchedEpisodes?.contains("S${state.selectedEpisodeDetails.season_number}E${state.selectedEpisodeDetails.episode_number}") == true,
                            onToggleWatched = { viewModel.toggleEpisode(state.selectedEpisodeDetails.season_number, state.selectedEpisodeDetails.episode_number) },
                            showTitle = state.mediaItem.name ?: state.mediaItem.title ?: "Unknown"
                        )
                    } else {
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
                                            
                                            val (isAired, countdownText) = try {
                                                if (episode.air_date.isNullOrEmpty()) {
                                                    Pair(true, "")
                                                } else {
                                                    val airDate = LocalDate.parse(episode.air_date)
                                                    val now = LocalDateTime.now()
                                                    val airDateTime = airDate.atTime(20, 0) // Assume 8 PM drop
                                                    
                                                    if (now.isAfter(airDateTime)) {
                                                        Pair(true, "")
                                                    } else {
                                                        val days = ChronoUnit.DAYS.between(now.toLocalDate(), airDate)
                                                        if (days > 0) {
                                                            Pair(false, "$days يوم")
                                                        } else {
                                                            val hours = ChronoUnit.HOURS.between(now, airDateTime)
                                                            if (hours > 0) {
                                                                Pair(false, "$hours ساعة")
                                                            } else {
                                                                val minutes = ChronoUnit.MINUTES.between(now, airDateTime)
                                                                Pair(false, "$minutes دقيقة")
                                                            }
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Pair(true, "")
                                            }
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkGrey)
                                                    .clickable { viewModel.selectEpisode(episode) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Image on the right (assuming RTL)
                                                AsyncImage(
                                                    model = episode.still_path?.let { "https://image.tmdb.org/t/p/w300$it" },
                                                    contentDescription = episode.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .width(100.dp)
                                                        .height(70.dp)
                                                        .background(Color.DarkGray)
                                                )
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                // Details in Middle
                                                Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                                                    val seasonNumberStr = String.format("%02d", episode.season_number)
                                                    val episodeNumberStr = String.format("%02d", episode.episode_number)
                                                    Text(
                                                        text = "S$seasonNumberStr | E$episodeNumberStr",
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = episode.name,
                                                        color = TextSecondary,
                                                        fontSize = 12.sp,
                                                        lineHeight = 16.sp,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                // Checkmark on the left (assuming RTL)
                                                if (isAired) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(end = 16.dp)
                                                            .size(32.dp)
                                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                                            .background(if (isWatched) Color(0xFF81C784) else Color.Transparent)
                                                            .border(2.dp, if (isWatched) Color(0xFF81C784) else TextSecondary, androidx.compose.foundation.shape.CircleShape)
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
                                                } else {
                                                    Text(
                                                        text = countdownText,
                                                        color = GoldYellow,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(end = 16.dp)
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
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = TrueBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                if (uiState is DetailsUiState.Success && (uiState as DetailsUiState.Success).isInWatchlist) {
                    Text(
                        text = "تتم المشاهدة",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider(
                        color = GoldYellow,
                        thickness = 2.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                val menuItems = listOf(
                    Pair("تخصيص", Icons.Default.Edit),
                    Pair("مفضلة", Icons.Default.FavoriteBorder),
                    Pair("إضافة إلى قائمة", Icons.Default.PlaylistAdd),
                    Pair("شاهد لاحقًا", Icons.Default.Schedule),
                    Pair("إيقاف المشاهدة", Icons.Default.Close),
                    Pair("إزالة العرض", Icons.Default.Remove),
                    Pair("مشاركة", Icons.Default.Share)
                )
                menuItems.forEach { (text, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (text == "إزالة العرض" && uiState is DetailsUiState.Success) {
                                    val state = uiState as DetailsUiState.Success
                                    if (state.isInWatchlist) {
                                        viewModel.toggleWatchlist()
                                    }
                                    showBottomSheet = false
                                } else {
                                    showBottomSheet = false
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = text, color = TextPrimary, fontSize = 16.sp)
                        Icon(icon, contentDescription = text, tint = TextPrimary)
                    }
                    if (text != "مشاركة") {
                        HorizontalDivider(color = DarkGrey)
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
        HorizontalDivider(color = DarkGrey)
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
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EpisodeDetailsContent(
    episode: com.example.data.remote.Episode,
    isWatched: Boolean,
    onToggleWatched: () -> Unit,
    showTitle: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Watched Toggle Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(if (isWatched) GoldYellow else DarkGrey)
                    .clickable { onToggleWatched() },
                contentAlignment = Alignment.Center
            ) {
                if (isWatched) {
                    Icon(Icons.Default.Check, contentDescription = "Watched", tint = TrueBlack, modifier = Modifier.size(28.dp))
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isWatched) "تمت المشاهدة" else "لم يُشاهد", color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                val date = episode.air_date ?: "غير معروف"
                Text(text = date, color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = DarkGrey)
        
        // Where to watch
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "أين تُشاهد", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = ". غير متاح", color = TextSecondary, fontSize = 14.sp)
        }
        
        HorizontalDivider(color = DarkGrey)
        
        // Episode Information
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "معلومات الحلقة", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rating = episode.vote_average?.let { String.format("%.1f", it) } ?: "0.0"
                Text(text = "$rating/10", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(5) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = episode.overview.orEmpty().ifEmpty { "لا توجد قصة متاحة للحلقة." },
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
