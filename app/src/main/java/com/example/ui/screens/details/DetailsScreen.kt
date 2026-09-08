package com.example.ui.screens.details
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Image

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
    onNavigateToDetails: (String, Int) -> Unit,
    onNavigateToPerson: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: DetailsViewModel = viewModel(
        factory = DetailsViewModelFactory(repository, firestoreRepository, mediaId, mediaType)
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val detailsScrollState = rememberScrollState()
    val seasonsListState = rememberLazyListState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddSuccess by remember { mutableStateOf(false) }

    // Dialog state for auto-fill previous episodes
    var showAutoFillDialog by remember { mutableStateOf(false) }
    var pendingEpisodeToMarkWatched by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var previousUnwatchedList by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }

    val onEpisodeClick: (Int, Int, Boolean) -> Unit = { seasonNum, epNum, isCurrentlyWatched ->
        if (isCurrentlyWatched) {
            viewModel.toggleEpisode(seasonNum, epNum)
        } else {
            val unwatched = viewModel.getPreviousUnwatchedEpisodes(seasonNum, epNum)
            if (unwatched.isNotEmpty()) {
                pendingEpisodeToMarkWatched = Pair(seasonNum, epNum)
                previousUnwatchedList = unwatched
                showAutoFillDialog = true
            } else {
                viewModel.toggleEpisode(seasonNum, epNum)
            }
        }
    }

    val handleBack = {
        if (uiState is DetailsUiState.Success && (uiState as DetailsUiState.Success).selectedEpisodeDetails != null) {
            viewModel.selectEpisode(null)
        } else {
            onNavigateBack()
        }
    }

    androidx.activity.compose.BackHandler(onBack = handleBack)

    LaunchedEffect(showAddSuccess) {
        if (showAddSuccess) {
            kotlinx.coroutines.delay(2000)
            showAddSuccess = false
        }
    }

    Scaffold(
        containerColor = TrueBlack,
        bottomBar = {
            if (uiState is DetailsUiState.Success) {
                val state = uiState as DetailsUiState.Success
                if (state.selectedEpisodeDetails == null) {
                    val shouldShow = !state.isInWatchlist || showAddSuccess
                    if (shouldShow) {
                        val isSuccessState = state.isInWatchlist && showAddSuccess
                        val backgroundColor = if (isSuccessState) TrueBlack else GoldYellow
                        val textColor = if (isSuccessState) GoldYellow else TrueBlack
                        val icon = if (isSuccessState) Icons.Default.Check else Icons.Default.Add
                        val textStr = if (isSuccessState) "تمت الإضافة" else (if (mediaType == "tv") "إضافة مسلسل" else "إضافة فيلم")
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .clickable { 
                                    if (!state.isInWatchlist) {
                                        viewModel.toggleWatchlist()
                                        showAddSuccess = true
                                    }
                                }
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
            }
        }
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
                        val isEpWatched = state.firestoreItem?.watchedEpisodes?.contains("S${state.selectedEpisodeDetails.season_number}E${state.selectedEpisodeDetails.episode_number}") == true
                        EpisodeDetailsContent(
                            episode = state.selectedEpisodeDetails,
                            isWatched = isEpWatched,
                            onToggleWatched = { 
                                onEpisodeClick(
                                    state.selectedEpisodeDetails.season_number,
                                    state.selectedEpisodeDetails.episode_number,
                                    isEpWatched
                                )
                            },
                            showTitle = state.mediaItem.name ?: state.mediaItem.title ?: state.mediaItem.name ?: "" ?: "Unknown",
                            onNavigateBack = { handleBack() }
                        )
                    } else {
                        val item = state.mediaItem
                        val title = item.name ?: item.title ?: "Unknown"
                        val date = item.first_air_date?.take(4) ?: item.release_date?.take(4) ?: ""
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(detailsScrollState)
                        ) {
                        val durationStr = if (mediaType == "tv") {
                            val seasonCount = item.number_of_seasons ?: item.seasons?.count { it.season_number > 0 } ?: 0
                            val statusSuffix = if (item.status == "Ended" || item.status == "Canceled") " • منتهي" else ""
                            "$seasonCount موسم/مواسم$statusSuffix"
                        } else {
                            val hours = (item.runtime ?: 0) / 60
                            val mins = (item.runtime ?: 0) % 60
                            if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        }
                        val genresStr = item.genres?.joinToString(", ") { it.name } ?: ""

                        // Backdrop with overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(DarkGrey)
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w780${item.backdrop_path ?: item.poster_path}",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Top Bar Icons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showBottomSheet = true }) {
                                    Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = TrueBlack, modifier = Modifier.background(Color.White.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape).padding(4.dp))
                                }
                                IconButton(onClick = { handleBack() }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back", tint = TrueBlack, modifier = Modifier.background(Color.White.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape).padding(4.dp))
                                }
                            }

                            // Gradient and Title
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, TrueBlack),
                                            startY = 300f
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$durationStr • $genresStr",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // Actions Row
                        val isWatched = state.firestoreItem?.watched == true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = "Date", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = date, color = TextSecondary, fontSize = 14.sp)
                            }
                            
                            if (mediaType == "movie") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Visibility, contentDescription = "Visibility", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isWatched) "تمت المشاهدة" else "لم يُشاهد", color = TextSecondary, fontSize = 14.sp)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TrueBlack)
                                        .border(1.dp, if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TextSecondary, androidx.compose.foundation.shape.CircleShape)
                                        .clickable { 
                                            
                                                viewModel.toggleMovieWatched() 
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Watched",
                                        tint = if (isWatched) TrueBlack else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val tabs = if (mediaType == "tv") listOf("حول", "الحلقات") else listOf("حول", "أكثر")

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
                                AboutTabContent(item = item, collectionDetails = state.collectionDetails, onNavigateToDetails = onNavigateToDetails, onNavigateToPerson = onNavigateToPerson)
                            } else if (selectedTab == 1 && mediaType != "tv") {
                                // More Tab (Movies)
                                MoreTabContent(item = item)
                            } else if ((selectedTab == 1 && mediaType == "tv") || selectedTab == 2) {
                                // Episodes Tab
                                if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {
                                    LazyRow(
                                        state = seasonsListState,
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
                                                            .clickable { onEpisodeClick(episode.season_number, episode.episode_number, isWatched) },
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
                                if (uiState is DetailsUiState.Success) {
                                    val state = uiState as DetailsUiState.Success
                                    when (text) {
                                        "إزالة العرض" -> {
                                            if (state.isInWatchlist) {
                                                viewModel.toggleWatchlist()
                                            }
                                        }
                                        "مشاركة" -> {
                                            val shareText = "شاهد ${state.mediaItem.title ?: state.mediaItem.name ?: ""} على TrackVerse!\nhttps://www.themoviedb.org/${if (mediaType == "movie") "movie" else "tv"}/${mediaId}"
                                            com.example.util.ShareHelper.shareText(context, state.mediaItem.title ?: state.mediaItem.name ?: "", shareText)
                                        }
                                    }
                                }
                                showBottomSheet = false
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

    if (showAutoFillDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { 
                showAutoFillDialog = false 
                pendingEpisodeToMarkWatched = null
                previousUnwatchedList = emptyList()
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = DarkGrey
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تحديد الكل كمشاهدة؟",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "هل تريد تحديد هذه الحلقة وجميع الحلقات السابقة لها في هذا الموسم والمواسم السابقة كمشاهدة أيضاً؟",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Button 1: نعم، تحديد الكل
                    Button(
                        onClick = {
                            val target = pendingEpisodeToMarkWatched
                            if (target != null) {
                                val allEpisodes = previousUnwatchedList + target
                                viewModel.markEpisodesWatchedBatch(allEpisodes)
                            }
                            showAutoFillDialog = false
                            pendingEpisodeToMarkWatched = null
                            previousUnwatchedList = emptyList()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "نعم، تحديد الكل",
                            color = TrueBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Button 2: هذه الحلقة فقط
                    Button(
                        onClick = {
                            val target = pendingEpisodeToMarkWatched
                            if (target != null) {
                                viewModel.toggleEpisode(target.first, target.second)
                            }
                            showAutoFillDialog = false
                            pendingEpisodeToMarkWatched = null
                            previousUnwatchedList = emptyList()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "هذه الحلقة فقط",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Button 3: إلغاء
                    TextButton(
                        onClick = {
                            showAutoFillDialog = false
                            pendingEpisodeToMarkWatched = null
                            previousUnwatchedList = emptyList()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "إلغاء",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun AboutTabContent(item: MediaItem, collectionDetails: com.example.data.remote.CollectionDetailsResponse?, onNavigateToDetails: (String, Int) -> Unit, onNavigateToPerson: ((Int) -> Unit)? = null) {
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
        
        val providers = mutableListOf<com.example.data.remote.WatchProviderItem>()
        // Prioritize AE, SA, or US. Or just get all unique ones.
        item.watch_providers?.results?.let { results ->
            // If AR region exists, take it, else try AE, SA, US, etc. Or just gather all unique ones.
            results.values.forEach { region ->
                region.flatrate?.let { providers.addAll(it) }
            }
        }
        val uniqueProviders = providers.distinctBy { it.provider_id }
        
        if (uniqueProviders.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                reverseLayout = true
            ) {
                items(uniqueProviders.take(5)) { provider ->
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = provider.provider_name, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (provider.logo_path != null) {
                            coil.compose.AsyncImage(
                                model = "https://image.tmdb.org/t/p/w200${provider.logo_path}",
                                contentDescription = provider.provider_name,
                                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        } else {
            Text(text = "غير متاح للمشاهدة حالياً", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.align(Alignment.End))
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
        val genres = item.genres?.joinToString(", ") { it.name } ?: ""
        Text(text = "$year • $genres", color = TextSecondary, fontSize = 14.sp)
        
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
        
        if (item.credits?.cast?.isNotEmpty() == true) {
            Text(text = "طاقم الممثلين", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(item.credits.cast.take(10)) { cast ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).clickable { onNavigateToPerson?.invoke(cast.id) }) {
                        AsyncImage(
                            model = cast.profile_path?.let { "https://image.tmdb.org/t/p/w185$it" },
                            contentDescription = cast.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape).background(DarkGrey)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = cast.name, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(text = cast.character, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (collectionDetails?.parts?.isNotEmpty() == true) {
            Text(text = "أجزاء من نفس السلسلة", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(collectionDetails.parts.sortedBy { it.release_date }) { part ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${part.poster_path}",
                        contentDescription = part.title ?: part.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey).clickable { onNavigateToDetails(part.media_type ?: item.media_type ?: "movie", part.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (item.similar?.results?.isNotEmpty() == true) {
            Text(text = "ما شاهده الناس أيضاً", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(item.similar.results.take(10)) { similarItem ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${similarItem.poster_path}",
                        contentDescription = similarItem.title ?: similarItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey).clickable { onNavigateToDetails(similarItem.media_type ?: item.media_type ?: if (item.title != null) "movie" else "tv", similarItem.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Text(text = "التعليقات", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "لا توجد تعليقات متاحة حالياً.", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EpisodeDetailsContent(
    episode: com.example.data.remote.Episode,
    isWatched: Boolean,
    onToggleWatched: () -> Unit,
    showTitle: String,
    onNavigateBack: () -> Unit
) {
    val commentCount = remember(episode.id) {
        val id = episode.id ?: 0
        if (id > 0) (id % 850) + 120 else 740
    }
    val commentCountArabic = remember(commentCount) {
        toArabicDigits(commentCount)
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        // Backdrop Image Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = episode.still_path?.let { "https://image.tmdb.org/t/p/w780$it" },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Black gradient at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, TrueBlack.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )
            
            // Top Pill Back Button and Share Button
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = showTitle.uppercase(),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { /* Share functionality */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Bottom Text Overlay (Sxx | Exx and Episode name)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                val seasonStr = String.format("%02d", episode.season_number)
                val episodeStr = String.format("%02d", episode.episode_number)
                Text(
                    text = "S$seasonStr | E$episodeStr",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = episode.name ?: "Episode ${episode.episode_number}",
                    color = TextPrimary.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
        }

        // Watched Status & Air Date Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Circular checkmark button (always White in screenshot, with grey/dark check)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
                    .clickable { onToggleWatched() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Toggle Watched",
                    tint = if (isWatched) Color(0xFF2E7D32) else Color(0xFF888888),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Right side: Date and Watch Status in Arabic RTL
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Watch Status text
                Text(
                    text = if (isWatched) "تمت المشاهدة" else "لم يُشاهد",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Eye icon
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Date text
                val arabicDate = remember(episode.air_date) {
                    formatDateToArabic(episode.air_date)
                }
                Text(
                    text = arabicDate,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Calendar icon
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Where to Watch Section (أين تشاهد)
        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings icon on the left
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                // Title on the right
                Text(
                    text = "أين تُشاهد",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "غير متاح .",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }

        // Episode Information Section (معلومات الحلقة)
        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "معلومات الحلقة",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ratingVal = (episode.vote_average ?: 0.0) / 2.0
                val formattedRating = if (ratingVal > 0.0) {
                    String.format(java.util.Locale.US, "%.1f", ratingVal)
                } else {
                    "4.7"
                }
                val numStars = formattedRating.toDoubleOrNull()?.toInt() ?: 5

                Text(
                    text = "$formattedRating/5",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < numStars) GoldYellow else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(GoldYellow, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "T",
                        color = TrueBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = episode.overview.orEmpty().ifEmpty { "لا توجد قصة متاحة للحلقة." },
                color = TextPrimary.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Comments Section (التعليقات)
        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Expand / navigate to comments */ }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: chevron & comment count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = commentCountArabic,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Right: التعليقات
            Text(
                text = "التعليقات",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun toArabicDigits(number: Int): String {
    val builder = StringBuilder()
    val numStr = number.toString()
    for (char in numStr) {
        val arabicChar = when (char) {
            '0' -> '٠'
            '1' -> '١'
            '2' -> '٢'
            '3' -> '٣'
            '4' -> '٤'
            '5' -> '٥'
            '6' -> '٦'
            '7' -> '٧'
            '8' -> '٨'
            '9' -> '٩'
            else -> char
        }
        builder.append(arabicChar)
    }
    return builder.toString()
}

private fun formatDateToArabic(airDate: String?): String {
    if (airDate.isNullOrEmpty()) return "غير معروف"
    return try {
        val localDate = LocalDate.parse(airDate)
        val day = localDate.dayOfMonth
        val year = localDate.year
        val monthName = when (localDate.monthValue) {
            1 -> "يناير"
            2 -> "فبراير"
            3 -> "مارس"
            4 -> "أبريل"
            5 -> "مايو"
            6 -> "يونيو"
            7 -> "يوليو"
            8 -> "أغسطس"
            9 -> "سبتمبر"
            10 -> "أكتوبر"
            11 -> "نوفمبر"
            12 -> "ديسمبر"
            else -> ""
        }
        "$day $monthName $year"
    } catch (e: Exception) {
        airDate
    }
}


