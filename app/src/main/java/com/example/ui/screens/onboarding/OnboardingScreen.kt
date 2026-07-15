package com.example.ui.screens.onboarding

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0A0C) // Sleek deep dark background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Horizontal Pager for 3 steps
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingStepPage(
                        illustration = { WelcomeIllustration(active = pagerState.currentPage == 0) },
                        title = "Welcome to TMDB Tracker",
                        description = "Track your favorite movies and TV shows, manage what you watch, and discover new content easily."
                    )
                    1 -> OnboardingStepPage(
                        illustration = { ProgressTrackerIllustration(active = pagerState.currentPage == 1) },
                        title = "Track Your Progress",
                        description = "Never lose track of which episode you're on. Easily mark episodes as watched and see your progress."
                    )
                    2 -> OnboardingStepPage(
                        illustration = { CommunityIllustration(active = pagerState.currentPage == 2) },
                        title = "Custom Lists & Community",
                        description = "Create your own custom lists, share them with friends, and interact with the community."
                    )
                }
            }

            // Bottom control navigation area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    // Page indicator: 3 dots at the bottom center. Active expands to blue capsule.
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val active = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (active) 24.dp else 8.dp,
                                label = "dot_width"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(if (active) Color(0xFF00A3FF) else Color.Gray.copy(alpha = 0.5f))
                            )
                        }
                    }

                    // Bottom-right Action Button: blue rounded-square FAB
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(56.dp)
                            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                            .background(Color(0xFF00A3FF), shape = RoundedCornerShape(16.dp))
                            .clickable {
                                coroutineScope.launch {
                                    if (pagerState.currentPage < 2) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    } else {
                                        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
                                        onComplete()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (pagerState.currentPage == 2) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = if (pagerState.currentPage == 2) "Complete" else "Next",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepPage(
    illustration: @Composable () -> Unit,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Space for Illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title text
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 0.5.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description text
        Text(
            text = description,
            color = Color(0xFFAAAAAA),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.25.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

// ==========================================
// SCREEN 1 ILLUSTRATION: Overlapping fanned-out movie posters
// ==========================================
@Composable
fun WelcomeIllustration(active: Boolean) {
    val leftOffset by animateDpAsState(
        targetValue = if (active) (-65).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "left_offset"
    )
    val rightOffset by animateDpAsState(
        targetValue = if (active) 65.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "right_offset"
    )
    val leftRotation by animateFloatAsState(
        targetValue = if (active) -12f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "left_rot"
    )
    val rightRotation by animateFloatAsState(
        targetValue = if (active) 12f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "right_rot"
    )

    // Infinite float animations
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_float")
    val floatLeftY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_left_y"
    )
    val floatRightY by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_right_y"
    )
    val floatCenterY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_center_y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left Poster (Deadpool)
        PosterCard(
            title = "Deadpool",
            imageUrl = "https://image.tmdb.org/t/p/w500/30c5jO7YEXuF8KiWXLg9m28GWDA.jpg",
            gradientColors = listOf(Color(0xFF800000), Color(0xFF1E0000)),
            modifier = Modifier
                .offset(x = leftOffset, y = 15.dp + floatLeftY.dp)
                .graphicsLayer { rotationZ = leftRotation }
                .zIndex(1f)
        )

        // Right Poster (The Godfather)
        PosterCard(
            title = "The Godfather",
            imageUrl = "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
            gradientColors = listOf(Color(0xFF3E2723), Color(0xFF1A0F0D)),
            modifier = Modifier
                .offset(x = rightOffset, y = 15.dp + floatRightY.dp)
                .graphicsLayer { rotationZ = rightRotation }
                .zIndex(2f)
        )

        // Center Poster (The Dark Knight)
        PosterCard(
            title = "The Dark Knight",
            imageUrl = "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF020617)),
            modifier = Modifier
                .offset(y = (-5).dp + floatCenterY.dp)
                .graphicsLayer { rotationZ = 0f }
                .zIndex(3f)
        )
    }
}

@Composable
fun PosterCard(
    title: String,
    imageUrl: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(180.dp)
            .shadow(12.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Base layer for fallback / design
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
            )
            
            // Poster Image
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            // Text Overlay fallback / title display
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

// ==========================================
// SCREEN 2 ILLUSTRATION: Sleek layout episode tracker card
// ==========================================
@Composable
fun ProgressTrackerIllustration(active: Boolean) {
    // Progress starts at 2% (0.02f) and goes to 75% (0.75f) when active
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(active) {
        if (active) {
            startAnimation = true
        } else {
            startAnimation = false
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 0.75f else 0.02f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "progress_val"
    )

    // Entry scaling and spring motion
    val scale by animateFloatAsState(
        targetValue = if (active) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    // Gentle infinite floating
    val infiniteTransition = rememberInfiniteTransition(label = "tracker_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tracker_y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .padding(16.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = floatY.dp.toPx()
                }
                .shadow(16.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play button square icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF00A3FF), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Skeleton Lines & Subtitle text S02 E04
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Title skeleton line 1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(8.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                        )
                        // Title skeleton line 2
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(8.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Subtitle display S02 E04 in modern styled container
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "S02 E04",
                        color = Color(0xFF00A3FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Next Episode",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }

                // Progress Bar and Percentage Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00E0FF),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    
                    // Percentage Text
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3 ILLUSTRATION: Community Node Diagram
// ==========================================
@Composable
fun CommunityIllustration(active: Boolean) {
    // Entrance animations
    val scaleCenter by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale_center"
    )
    val scalePink by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "scale_pink"
    )
    val scalePurple by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "scale_purple"
    )
    val scaleGreen by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "scale_green"
    )

    // Infinite Float animations
    val infiniteTransition = rememberInfiniteTransition(label = "community_float")
    val floatPinkY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_pink"
    )
    val floatPurpleY by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_purple"
    )
    val floatGreenY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_green"
    )
    val pulseCenter by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_center"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle connecting data dots/lines
        Canvas(
            modifier = Modifier
                .width(280.dp)
                .height(240.dp)
        ) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            
            // Dynamic endpoints (base values match the offsets in layout)
            // Left-Top: Pink node (x = -70dp, y = -60dp)
            val pinkX = center.x - 70.dp.toPx()
            val pinkY = center.y - 60.dp.toPx() + floatPinkY.dp.toPx()
            val pinkOffset = androidx.compose.ui.geometry.Offset(
                center.x + (pinkX - center.x) * scalePink,
                center.y + (pinkY - center.y) * scalePink
            )

            // Right-Top: Purple node (x = 80dp, y = -40dp)
            val purpleX = center.x + 80.dp.toPx()
            val purpleY = center.y - 40.dp.toPx() + floatPurpleY.dp.toPx()
            val purpleOffset = androidx.compose.ui.geometry.Offset(
                center.x + (purpleX - center.x) * scalePurple,
                center.y + (purpleY - center.y) * scalePurple
            )

            // Center-Bottom: Green node (x = -10dp, y = 70dp)
            val greenX = center.x - 10.dp.toPx()
            val greenY = center.y + 70.dp.toPx() + floatGreenY.dp.toPx()
            val greenOffset = androidx.compose.ui.geometry.Offset(
                center.x + (greenX - center.x) * scaleGreen,
                center.y + (greenY - center.y) * scaleGreen
            )
            
            val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            
            if (scalePink > 0.05f) {
                drawLine(
                    color = Color(0xFF00A3FF).copy(alpha = 0.35f * scalePink),
                    start = center,
                    end = pinkOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect
                )
            }
            
            if (scalePurple > 0.05f) {
                drawLine(
                    color = Color(0xFF00A3FF).copy(alpha = 0.35f * scalePurple),
                    start = center,
                    end = purpleOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect
                )
            }
            
            if (scaleGreen > 0.05f) {
                drawLine(
                    color = Color(0xFF00A3FF).copy(alpha = 0.35f * scaleGreen),
                    start = center,
                    end = greenOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect
                )
            }
        }

        Box(
            modifier = Modifier
                .width(280.dp)
                .height(240.dp)
        ) {
            // Central blue glowing circle containing a white share icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scaleCenter * pulseCenter
                        scaleY = scaleCenter * pulseCenter
                    }
                    .shadow(16.dp, shape = CircleShape, ambientColor = Color(0xFF00A3FF), spotColor = Color(0xFF00A3FF))
                    .background(Color(0xFF00A3FF), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Pink circle "M"
            UserCircleNode(
                initial = "M",
                color = Color(0xFFE91E63),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 45.dp, y = 30.dp + floatPinkY.dp)
                    .graphicsLayer {
                        scaleX = scalePink
                        scaleY = scalePink
                    }
            )

            // Purple circle "A"
            UserCircleNode(
                initial = "A",
                color = Color(0xFF9C27B0),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-35).dp, y = 50.dp + floatPurpleY.dp)
                    .graphicsLayer {
                        scaleX = scalePurple
                        scaleY = scalePurple
                    }
            )

            // Green circle "K"
            UserCircleNode(
                initial = "K",
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = (-10).dp, y = (-25).dp + floatGreenY.dp)
                    .graphicsLayer {
                        scaleX = scaleGreen
                        scaleY = scaleGreen
                    }
            )
        }
    }
}

@Composable
fun UserCircleNode(
    initial: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(8.dp, shape = CircleShape)
            .background(color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
