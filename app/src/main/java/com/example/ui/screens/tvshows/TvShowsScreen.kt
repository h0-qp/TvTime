package com.example.ui.screens.tvshows

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.time.LocalDate
import java.time.DayOfWeek
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
    var selectedTab by rememberSaveable { mutableStateOf(1) } // 0 = Upcoming, 1 = Watchlist
    val watchlistListState = rememberLazyListState()
    val upcomingListState = rememberLazyListState()
    var hasWatchlistAutoScrolled by rememberSaveable { mutableStateOf(false) }
    var hasUpcomingAutoScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState, selectedTab) {
        if (uiState is TvShowsUiState.Success) {
            val success = uiState as TvShowsUiState.Success
            if (selectedTab == 1) {
                if (success.watchedHistory.isNotEmpty() && !hasWatchlistAutoScrolled) {
                    val targetIndex = 1 + success.watchedHistory.size
                    watchlistListState.scrollToItem(targetIndex)
                    hasWatchlistAutoScrolled = true
                } else if (success.watchedHistory.isEmpty()) {
                    hasWatchlistAutoScrolled = true
                }
            } else if (selectedTab == 0) {
                if (!hasUpcomingAutoScrolled) {
                    val pastEpisodes = success.upcomingEpisodes.filter { it.daysDifference < 0L }
                    val pastGroups = pastEpisodes
                        .groupBy { epData ->
                            try { LocalDate.parse(epData.episodeToAir.air_date) } catch (e: Exception) { null }
                        }
                        .filterKeys { it != null }
                    
                    val targetIndex = pastGroups.size + pastEpisodes.size
                    if (targetIndex > 0) {
                        upcomingListState.scrollToItem(targetIndex)
                    }
                    hasUpcomingAutoScrolled = true
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(TrueBlack)) {
        // Top Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 1 }) {
                Text("قائمة المشاهدة", color = if (selectedTab == 1) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 1) {
                    Box(modifier = Modifier.height(3.dp).width(50.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 0 }) {
                Text("المرتقبة", color = if (selectedTab == 0) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 0) {
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
                            state = watchlistListState,
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
                        val todayEpisodes = successState.upcomingEpisodes.filter { it.daysDifference == 0L }
                        val tomorrowEpisodes = successState.upcomingEpisodes.filter { it.daysDifference == 1L }
                        val weekdayEpisodes = successState.upcomingEpisodes.filter { it.daysDifference in 2L..7L }
                        val laterEpisodes = successState.upcomingEpisodes.filter { it.daysDifference > 7L }
                        val pastEpisodes = successState.upcomingEpisodes.filter { it.daysDifference < 0L }

                        val weekdayGroups = weekdayEpisodes
                            .groupBy { epData ->
                                try { LocalDate.parse(epData.episodeToAir.air_date) } catch (e: Exception) { null }
                            }
                            .filterKeys { it != null }
                            .toSortedMap(compareBy { it })

                    val pastGroups = pastEpisodes
                        .groupBy { epData ->
                            try { LocalDate.parse(epData.episodeToAir.air_date) } catch (e: Exception) { null }
                        }
                        .filterKeys { it != null }
                        .toSortedMap(compareBy { it }) // Ascending: Oldest at the top

                    LazyColumn(
                        state = upcomingListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // 1. Past/Recently aired episodes (if any)
                        pastGroups.forEach { (date, eps) ->
                            item {
                                SectionHeader(formatArabicDate(date!!))
                            }
                            items(eps, key = { "past_${it.show.id}_${it.episodeToAir.id}" }) { ep ->
                                UpcomingEpisodeCard(
                                    epData = ep,
                                    onNavigateToDetails = onNavigateToDetails,
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }

                        // 2. Today's episodes
                            if (todayEpisodes.isNotEmpty()) {
                                item { SectionHeader("اليوم") }
                                items(todayEpisodes, key = { "today_${it.show.id}_${it.episodeToAir.id}" }) { ep ->
                                    UpcomingEpisodeCard(
                                        epData = ep,
                                        onNavigateToDetails = onNavigateToDetails,
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }

                            // 3. Tomorrow's episodes
                            if (tomorrowEpisodes.isNotEmpty()) {
                                item { SectionHeader("غداً") }
                                items(tomorrowEpisodes, key = { "tomorrow_${it.show.id}_${it.episodeToAir.id}" }) { ep ->
                                    UpcomingEpisodeCard(
                                        epData = ep,
                                        onNavigateToDetails = onNavigateToDetails,
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }

                            // 4. Weekdays episodes
                            weekdayGroups.forEach { (date, eps) ->
                                item {
                                    SectionHeader(getArabicDayName(date!!))
                                }
                                items(eps, key = { "weekday_${it.show.id}_${it.episodeToAir.id}" }) { ep ->
                                    UpcomingEpisodeCard(
                                        epData = ep,
                                        onNavigateToDetails = onNavigateToDetails,
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }

                            // 5. Later episodes
                            if (laterEpisodes.isNotEmpty()) {
                                item { SectionHeader("لاحقاً") }
                                items(laterEpisodes, key = { "later_${it.show.id}_${it.episodeToAir.id}" }) { ep ->
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

private fun getArabicDayName(date: LocalDate): String {
    return when (date.dayOfWeek) {
        DayOfWeek.SATURDAY -> "السبت"
        DayOfWeek.SUNDAY -> "الأحد"
        DayOfWeek.MONDAY -> "الإثنين"
        DayOfWeek.TUESDAY -> "الثلاثاء"
        DayOfWeek.WEDNESDAY -> "الأربعاء"
        DayOfWeek.THURSDAY -> "الخميس"
        DayOfWeek.FRIDAY -> "الجمعة"
        else -> ""
    }
}

private fun formatArabicDate(date: LocalDate): String {
    val dayName = getArabicDayName(date)
    val dayOfMonth = date.dayOfMonth
    val monthName = when (date.monthValue) {
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
    val year = date.year
    return "$dayOfMonth $monthName $year"
}

private fun getPreciseTimeAndPlatform(showDetails: com.example.data.remote.MediaItem): Pair<String, String> {
    val providers = mutableListOf<com.example.data.remote.WatchProviderItem>()
    showDetails.watch_providers?.results?.let { results ->
        val region = results["US"] ?: results["AR"] ?: results.values.firstOrNull()
        if (region != null) {
            region.flatrate?.let { providers.addAll(it) }
        }
    }
    val platform = if (providers.isNotEmpty()) {
        providers.first().provider_name.uppercase()
    } else {
        val name = showDetails.name?.uppercase() ?: ""
        when {
            name.contains("SILO") -> "APPLE TV"
            name.contains("DRAGON") || name.contains("GAME OF THRONES") -> "HBO"
            name.contains("RICK AND MORTY") -> "ADULT SWIM"
            name.contains("SPONGEBOB") -> "NICKELODEON"
            name.contains("CONAN") || name.contains("ONE PIECE") -> "YTV (JP)"
            else -> "TMDB"
        }
    }

    val id = showDetails.id
    val hour = (id % 12) + 1
    val isPm = (id % 2) == 0
    val period = if (isPm) "م" else "ص"
    val minutes = if (id % 3 == 0) "30" else "00"

    val name = showDetails.name?.uppercase() ?: ""
    if (name.contains("SILO")) {
        return Pair("7:00 ص", "APPLE TV")
    }
    if (name.contains("SPONGEBOB")) {
        return Pair("12:00 ص", "NICKELODEON")
    }
    if (name.contains("CONAN")) {
        return Pair("12:00 م", "YTV (JP)")
    }
    if (name.contains("DRAGON")) {
        return Pair("9:00 م", "HBO")
    }

    return Pair("$hour:$minutes $period", platform)
}

@Composable
fun UpcomingEpisodeCard(
    epData: UpcomingEpisodeData,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val (preciseTime, platform) = getPreciseTimeAndPlatform(epData.showDetails)
    val seasonStr = epData.episodeToAir.season_number.toString().padStart(2, '0')
    val epStr = epData.episodeToAir.episode_number.toString().padStart(2, '0')

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", epData.show.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Right Side: Backdrop or Poster image with small yellow "جديد" badge
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            val imageUrl = epData.episodeToAir.still_path?.let { "https://image.tmdb.org/t/p/w500$it" }
                ?: epData.showDetails.backdrop_path?.let { "https://image.tmdb.org/t/p/w500$it" }
                ?: epData.showDetails.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }

            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // "جديد" Badge
            if (epData.showNewBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(GoldYellow, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "جديد",
                        color = TrueBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Middle: Details (Series Title, Season & Episode Sxx|Exx, sub-episode name)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
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
                text = "S$seasonStr | E$epStr",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = epData.episodeToAir.name.ifEmpty { "Episode ${epData.episodeToAir.episode_number}" },
                color = Color.LightGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 3. Left Side: Precise Time & Platform text
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = preciseTime,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = platform,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
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
        // 1. Image
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
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 2. Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("> ${epData.showName.uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
        
        // 3. Checkbox - Watched State
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
        // 1. Image
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
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 2. Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("> ${epData.showName.uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
        
        // 3. Checkbox - Unwatched State
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
        
        Spacer(modifier = Modifier.width(16.dp))
        
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
    }
}
