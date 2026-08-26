package com.example.ui.screens.profile

import android.Manifest
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import coil.compose.AsyncImage
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.FirestoreMediaItem
import com.example.data.firebase.UserProfile
import kotlinx.coroutines.launch
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack
import com.google.accompanist.permissions.ExperimentalPermissionsApi

fun Int.toArabicDigits(): String {
    return this.toString().map { char ->
        if (char in '0'..'9') {
            (char - '0' + 0x0660).toChar()
        } else {
            char
        }
    }.joinToString("")
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository,
    mediaRepository: com.example.data.repository.MediaRepository,
    onSignOut: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit,
    onNavigateToAllTvShows: () -> Unit,
    onNavigateToAllMovies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ProfileViewModelFactory(firestoreRepository, mediaRepository)
    )
    val totalEpisodesWatched by viewModel.totalEpisodesWatched.collectAsState()
    val tvTimeMinutes by viewModel.tvTimeMinutes.collectAsState()
    val tvShowsCount by viewModel.tvShowsCount.collectAsState()
    val moviesCount by viewModel.moviesCount.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val currentUser = authRepository.currentUser
    val email = currentUser?.email ?: "guest@trackverse.com"
    val context = LocalContext.current
    
    val displayUsername = userProfile?.username?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
    val displayName = userProfile?.displayName?.takeIf { it.isNotBlank() } ?: displayUsername
    val profilePicture = userProfile?.profilePictureUrl
    val bio = userProfile?.bio ?: ""
    
    var showEditSheet by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            com.example.util.NotificationHelper.showNotification(
                context,
                "إشعار تجريبي",
                "الإشعارات تعمل بنجاح!"
            )
        } else {
            Toast.makeText(context, "يجب السماح بالإشعارات", Toast.LENGTH_SHORT).show()
        }
    }
    
    val userMedia by firestoreRepository.observeUserMedia().collectAsState(initial = emptyList())
    val tvShows = userMedia.filter { it.mediaType == "tv" }
    val movies = userMedia.filter { it.mediaType == "movie" }

    // Use a static tmdb backdrop placeholder or gradient to mimic the Teen Titans cover in screenshot
    val coverImageUrl = "https://image.tmdb.org/t/p/w780/d1vMtdx5k9jIfx4N3yAym8U0x3j.jpg"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        // Top Cover Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Background Image
                AsyncImage(
                    model = coverImageUrl,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, TrueBlack),
                                startY = 300f
                            )
                        )
                )

                // Top icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Start in RTL is Right (Bell)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldYellow)
                            .clickable { Toast.makeText(context, "لا توجد إشعارات جديدة", Toast.LENGTH_SHORT).show() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TrueBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // End in RTL is Left (Dots)
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onSignOut() }
                    )
                }

                // Profile Info (Bottom Start in RTL -> Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar (Right)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, TrueBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profilePicture.isNullOrEmpty()) {
                            AsyncImage(
                                model = profilePicture,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.LightGray,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Username & Edit (Left of Avatar)
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (displayUsername != displayName) {
                            Text(
                                text = "@$displayUsername",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { showEditSheet = true }
                        ) {
                            Text(
                                text = "تعديل",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        if (bio.isNotBlank()) {
            item {
                Text(
                    text = bio,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Stats Row (Followers, Following, Comments)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, DarkGrey))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatColumn("متابعًا", "٠")
                VerticalDivider(modifier = Modifier.height(50.dp), color = DarkGrey)
                ProfileStatColumn("متابِعين", "٠")
                VerticalDivider(modifier = Modifier.height(50.dp), color = DarkGrey)
                ProfileStatColumn("تعليقات", "٠")
            }
        }

        // Statistics Section
        item {
            SectionHeader(title = "إحصائيات")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Right Card in RTL -> TV Time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkGrey, RoundedCornerShape(8.dp))
                        .padding(vertical = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.Default.Tv, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TV time", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val months = tvTimeMinutes / (60 * 24 * 30)
                            val days = (tvTimeMinutes % (60 * 24 * 30)) / (60 * 24)
                            val hours = (tvTimeMinutes % (60 * 24)) / 60
                            
                            TimeStat("الشهور", months.toArabicDigits())
                            TimeStat("أيام", days.toArabicDigits())
                            TimeStat("الساعات", hours.toArabicDigits())
                        }
                    }
                }

                // Left Card in RTL -> Episodes watched
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkGrey, RoundedCornerShape(8.dp))
                        .padding(vertical = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.Default.Tv, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الحلقات المشاهدة مسبقا", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalEpisodesWatched).map { char -> if (char in '0'..'9') (char - '0' + 0x0660).toChar() else char }.joinToString(""),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Lists Section
        item {
            SectionHeader(title = "القوائم")
            AddButtonBox(text = "إنشاء قائمة جديدة")
        }

        // TV Shows Section
        item {
            SectionHeader(title = "مسلسلات", onClick = onNavigateToAllTvShows)
            if (tvShows.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tvShows.take(5)) { show ->
                        MediaPosterItem(show, onNavigateToDetails)
                    }
                }
            } else {
                Text(
                    text = "لا توجد مسلسلات",
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Favorite TV Shows Section
        item {
            SectionHeader(title = "المسلسلات المفضلة", isFavorite = true)
            AddButtonBox(text = "إضافة البرامج المفضلة")
        }

        // Movies Section
        item {
            SectionHeader(title = "أفلام", onClick = onNavigateToAllMovies)
            if (movies.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movies.take(5)) { movie ->
                        MediaPosterItem(movie, onNavigateToDetails)
                    }
                }
            } else {
                Text(
                    text = "لا توجد أفلام",
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Favorite Movies Section
        item {
            SectionHeader(title = "الأفلام المفضلة", isFavorite = true)
            AddButtonBox(text = "إضافة الأفلام المفضلة")
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        com.example.util.NotificationHelper.showNotification(
                            context,
                            "إشعار تجريبي",
                            "الإشعارات تعمل بنجاح!"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGrey)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Test Notifications", tint = GoldYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تجربة الإشعارات", color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        var editDisplayName by remember { mutableStateOf(displayName) }
        var editUsername by remember { mutableStateOf(displayUsername) }
        var editBio by remember { mutableStateOf(bio) }
        var editProfilePicture by remember { mutableStateOf(profilePicture ?: "") }
        var isSaving by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF121212),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "تعديل الملف الشخصي", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { 
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showEditSheet = false
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Profile Picture Editor
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DarkGrey)
                        .border(2.dp, GoldYellow, CircleShape)
                        .clickable {
                            // Quick toggle for cinematic avatars
                            val avatars = listOf(
                                "https://image.tmdb.org/t/p/w200/9U9Y5GQuWX3EZy39B8nkk4NY01S.jpg", // Walter White
                                "https://image.tmdb.org/t/p/w200/lOrnB1iID2O7mJmS1bN8m6c1b3Z.jpg", // Jon Snow
                                "https://image.tmdb.org/t/p/w200/1XjdO9Jq9uS8r33w664XqX9P9y9.jpg", // Eleven
                                "https://image.tmdb.org/t/p/w200/5v5wK85D2b4q5E2m3XbK658Fq67.jpg", // Geralt
                                "https://image.tmdb.org/t/p/w200/8qBylBsQf4llkGrZA3Ww8aL9k4D.jpg", // Mandalorian
                                "https://image.tmdb.org/t/p/w200/3oWEuo0eHWFCE5N56P3W0c17GvU.jpg" // John Wick
                            )
                            val currentIndex = avatars.indexOf(editProfilePicture)
                            editProfilePicture = avatars[(currentIndex + 1) % avatars.size]
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (editProfilePicture.isNotBlank()) {
                        AsyncImage(
                            model = editProfilePicture,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.LightGray, modifier = Modifier.size(60.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(GoldYellow)
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Edit Picture", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = editDisplayName,
                    onValueChange = { editDisplayName = it },
                    label = { Text("الاسم الظاهر", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = DarkGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = editUsername,
                    onValueChange = { editUsername = it },
                    label = { Text("اسم المستخدم (@)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = DarkGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = editBio,
                    onValueChange = { if (it.length <= 150) editBio = it },
                    label = { Text("النبذة الشخصية", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = DarkGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )
                Text("${editBio.length} / 150", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        isSaving = true
                        val newProfile = UserProfile(
                            displayName = editDisplayName.trim(),
                            username = editUsername.trim().removePrefix("@"),
                            bio = editBio.trim(),
                            profilePictureUrl = editProfilePicture
                        )
                        viewModel.updateUserProfile(newProfile) {
                            isSaving = false
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showEditSheet = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("حفظ التغييرات", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileStatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
fun TimeStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun SectionHeader(title: String, isFavorite: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (isFavorite) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "View All",
            tint = Color.White
        )
    }
}

@Composable
fun AddButtonBox(text: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .clickable { Toast.makeText(context, "هذه الميزة ستتوفر قريباً", Toast.LENGTH_SHORT).show() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun MediaPosterItem(item: FirestoreMediaItem, onNavigateToDetails: (String, Int) -> Unit) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGrey)
            .clickable { onNavigateToDetails(item.mediaType, item.id) }
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w342${item.posterPath}",
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (item.watched) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Watched",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
