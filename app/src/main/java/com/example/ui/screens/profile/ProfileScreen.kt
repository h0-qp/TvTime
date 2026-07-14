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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.FirestoreMediaItem
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository,
    onSignOut: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = authRepository.currentUser
    val email = currentUser?.email ?: "guest@trackverse.com"
    val context = LocalContext.current
    val username = email.substringBefore("@")
    
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
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.LightGray,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Username & Edit (Left of Avatar)
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = username,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { Toast.makeText(context, "هذه الميزة ستتوفر قريباً", Toast.LENGTH_SHORT).show() }
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
                ProfileStatColumn("متابعًا", "١")
                VerticalDivider(modifier = Modifier.height(50.dp), color = DarkGrey)
                ProfileStatColumn("متابِعين", "٢")
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
                            TimeStat("الشهور", "٤")
                            TimeStat("أيام", "٨")
                            TimeStat("الساعات", "٢")
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
                            text = "٦,٦٧٧",
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
            SectionHeader(title = "مسلسلات")
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
            SectionHeader(title = "أفلام")
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
fun SectionHeader(title: String, isFavorite: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
    }
}
