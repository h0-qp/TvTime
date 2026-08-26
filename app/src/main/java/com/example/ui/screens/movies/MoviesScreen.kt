package com.example.ui.screens.movies

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.remote.MediaItem
import com.example.data.repository.MediaRepository
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MovieWithDetails(
    val firebaseItem: FirestoreMediaItem,
    val tmdbItem: MediaItem?,
    val daysDiff: Long?,
    val formattedDate: String,
    val runtimeStr: String,
    val genresStr: String
)

fun getDaysDifference(releaseDateStr: String?): Long? {
    if (releaseDateStr.isNullOrEmpty()) return null
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val releaseDate = dateFormat.parse(releaseDateStr) ?: return null
        
        val calToday = Calendar.getInstance()
        calToday.set(Calendar.HOUR_OF_DAY, 0)
        calToday.set(Calendar.MINUTE, 0)
        calToday.set(Calendar.SECOND, 0)
        calToday.set(Calendar.MILLISECOND, 0)
        
        val calRelease = Calendar.getInstance()
        calRelease.time = releaseDate
        calRelease.set(Calendar.HOUR_OF_DAY, 0)
        calRelease.set(Calendar.MINUTE, 0)
        calRelease.set(Calendar.SECOND, 0)
        calRelease.set(Calendar.MILLISECOND, 0)
        
        val diffInMillis = calRelease.timeInMillis - calToday.timeInMillis
        diffInMillis / (1000 * 60 * 60 * 24)
    } catch (e: Exception) {
        null
    }
}

fun formatReleaseDateArabic(releaseDateStr: String?): String {
    if (releaseDateStr.isNullOrEmpty()) return "غير مؤكد"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(releaseDateStr) ?: return "غير مؤكد"
        
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        val formatted = outputFormat.format(date)
        
        formatted.map { char ->
            when (char) {
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
        }.joinToString("")
    } catch (e: Exception) {
        "غير مؤكد"
    }
}

fun formatRuntime(runtime: Int?): String {
    if (runtime == null || runtime == 0) return ""
    val hours = runtime / 60
    val minutes = runtime % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
fun MoviesScreen(
    repository: MediaRepository,
    firestoreRepository: com.example.data.firebase.FirestoreRepository,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: MoviesViewModel = viewModel(
        factory = MoviesViewModelFactory(repository, firestoreRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(1) } // 0 = Upcoming (المرتقبة), 1 = Watchlist (قائمة المشاهدة)
    var isGridView by rememberSaveable { mutableStateOf(false) }

    val watchlistListState = rememberLazyListState()
    val watchlistGridState = rememberLazyGridState()
    val upcomingListState = rememberLazyListState()
    val upcomingGridState = rememberLazyGridState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Top Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 1 }
            ) {
                Text(
                    text = "قائمة المشاهدة",
                    color = if (selectedTab == 1) GoldYellow else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 1) {
                    Box(modifier = Modifier.height(3.dp).width(100.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 0 }
            ) {
                Text(
                    text = "المرتقبة",
                    color = if (selectedTab == 0) GoldYellow else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (selectedTab == 0) {
                    Box(modifier = Modifier.height(3.dp).width(60.dp).background(GoldYellow, RoundedCornerShape(1.5.dp)))
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }

        // View Toggle Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Right Toggle Icon Button (في أعلى الشاشة جهة اليمين لتشابه التصميم الأصلي)
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (isGridView) GoldYellow else Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Toggle View",
                    tint = if (isGridView) GoldYellow else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is MoviesUiState.Loading -> {
                    CircularProgressIndicator(color = GoldYellow)
                }
                is MoviesUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                is MoviesUiState.Success -> {
                    // Precompute and sort lists
                    val moviesWithDetails = state.watchlist.map { movie ->
                        val details = state.movieDetails[movie.id]
                        val daysDiff = getDaysDifference(details?.release_date)
                        val formattedDate = formatReleaseDateArabic(details?.release_date)
                        val runtimeStr = formatRuntime(details?.runtime)
                        val genresStr = details?.genres?.joinToString(", ") { it.name }.orEmpty()
                        MovieWithDetails(movie, details, daysDiff, formattedDate, runtimeStr, genresStr)
                    }

                    if (selectedTab == 1) {
                        // Watchlist Screen
                        if (state.watchlist.isEmpty()) {
                            Text("قائمتك فارغة. ابحث عن أفلام لإضافتها!", color = TextSecondary, textAlign = TextAlign.Center)
                        } else {
                            Crossfade(targetState = isGridView, animationSpec = tween(300)) { grid ->
                                if (grid) {
                                    LazyVerticalGrid(
                                        state = watchlistGridState,
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.watchlist) { movie ->
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .aspectRatio(2f / 3f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.DarkGray)
                                                    .clickable { onNavigateToDetails("movie", movie.id) }
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = watchlistListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        items(moviesWithDetails, key = { it.firebaseItem.id }) { movieItem ->
                                            MovieWatchlistCard(
                                                movie = movieItem,
                                                onNavigateToDetails = onNavigateToDetails,
                                                onMarkWatched = { viewModel.markAsWatched(it) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Upcoming Screen
                        val pastMovies = moviesWithDetails
                            .filter { it.daysDiff != null && it.daysDiff < 0 }
                            .sortedByDescending { it.daysDiff } // chronological order (recent past first)

                        val futureAndUndatedMovies = moviesWithDetails
                            .filter { it.daysDiff == null || it.daysDiff >= 0 }
                            .sortedWith(compareBy<MovieWithDetails> { it.daysDiff == null }
                                .thenBy { it.daysDiff ?: Long.MAX_VALUE })

                        val groupedPastMovies = pastMovies.groupBy { it.formattedDate }

                        if (moviesWithDetails.isEmpty()) {
                            Text("لا توجد أفلام مرتقبة في قائمتك حالياً.", color = TextSecondary, textAlign = TextAlign.Center)
                        } else {
                            Crossfade(targetState = isGridView, animationSpec = tween(300)) { grid ->
                                if (grid) {
                                    LazyVerticalGrid(
                                        state = upcomingGridState,
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (futureAndUndatedMovies.isNotEmpty()) {
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                SectionHeader("لاحقاً")
                                            }
                                            items(futureAndUndatedMovies) { movieItem ->
                                                UpcomingMovieGridCard(movieItem, onNavigateToDetails)
                                            }
                                        }

                                        groupedPastMovies.forEach { (date, moviesInDate) ->
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                SectionHeader(date)
                                            }
                                            items(moviesInDate) { movieItem ->
                                                UpcomingMovieGridCard(movieItem, onNavigateToDetails)
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = upcomingListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        if (futureAndUndatedMovies.isNotEmpty()) {
                                            item {
                                                SectionHeader("لاحقاً")
                                            }
                                            items(futureAndUndatedMovies) { movieItem ->
                                                UpcomingMovieCard(movieItem, onNavigateToDetails)
                                            }
                                        }

                                        groupedPastMovies.forEach { (date, moviesInDate) ->
                                            item {
                                                SectionHeader(date)
                                            }
                                            items(moviesInDate) { movieItem ->
                                                UpcomingMovieCard(movieItem, onNavigateToDetails)
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
fun MovieWatchlistCard(
    movie: MovieWithDetails,
    onNavigateToDetails: (String, Int) -> Unit,
    onMarkWatched: (FirestoreMediaItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("movie", movie.firebaseItem.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Right side: Poster
        if (movie.firebaseItem.posterPath != null) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.firebaseItem.posterPath}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )
        } else {
            // Default blue placeholder with Tv Icon
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 2. Center: Details (Title, runtime, genres)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = movie.firebaseItem.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val detailsText = buildString {
                if (movie.runtimeStr.isNotEmpty()) {
                    append(movie.runtimeStr)
                }
                if (movie.genresStr.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append(movie.genresStr)
                }
            }
            if (detailsText.isNotEmpty()) {
                Text(
                    text = detailsText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 3. Left side: Check button (صح فارغ يمتلئ بالضغط)
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(2.dp, Color.White, CircleShape)
                .clip(CircleShape)
                .clickable { onMarkWatched(movie.firebaseItem) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Mark as Watched",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun UpcomingMovieCard(
    movie: MovieWithDetails,
    onNavigateToDetails: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("movie", movie.firebaseItem.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Right side: Poster
        if (movie.firebaseItem.posterPath != null) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.firebaseItem.posterPath}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )
        } else {
            // Default blue placeholder with Tv Icon
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Center: Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = movie.firebaseItem.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(4.dp))
            val detailsText = buildString {
                if (movie.runtimeStr.isNotEmpty()) {
                    append(movie.runtimeStr)
                }
                if (movie.genresStr.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append(movie.genresStr)
                }
            }
            if (detailsText.isNotEmpty()) {
                Text(
                    text = detailsText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 3. Left side: Days counter
        if (movie.daysDiff != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = if (movie.daysDiff < 0) "${-movie.daysDiff}-" else "${movie.daysDiff}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "أيام",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }
    }
}

@Composable
fun UpcomingMovieGridCard(
    movieItem: MovieWithDetails,
    onNavigateToDetails: (String, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray)
            .clickable { onNavigateToDetails("movie", movieItem.firebaseItem.id) }
    ) {
        if (movieItem.firebaseItem.posterPath != null) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movieItem.firebaseItem.posterPath}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        // Days counter overlay for future/upcoming movies (bottom-left)
        if (movieItem.daysDiff != null && movieItem.daysDiff >= 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${movieItem.daysDiff} أيام",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
