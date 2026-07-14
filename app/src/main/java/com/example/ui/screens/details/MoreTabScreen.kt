package com.example.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.remote.MediaItem
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreTabContent(item: MediaItem) {
    val viewModel: MoreTabViewModel = viewModel(
        key = item.id.toString(),
        factory = MoreTabViewModelFactory(item.id.toString())
    )
    val uiState by viewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            android.widget.Toast.makeText(context, "خطأ في قاعدة البيانات: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val stats = uiState.stats
    val userVotes = uiState.userVotes

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp) // space for FAB
        ) {
            // Section 1: Where did you watch?
            SectionTitle("أين شاهدت الحلقة؟")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                val platforms = listOf(
                    PlatformItem("غير رسمي", "unofficial", "🥸"), // Using emojis as placeholder for icons
                    PlatformItem("أخرى", "other", "💬"),
                    PlatformItem("THEATER", "theater", "🎬")
                )
                platforms.forEach { platform ->
                    val isSelected = userVotes.platform == platform.id
                    val votes = stats.platforms[platform.id] ?: 0L
                    val total = stats.totalVotesPlatforms
                    val percentage = if (total > 0L) ((votes * 100L) / total).toInt() else 0

                    PlatformCard(
                        platform = platform,
                        percentage = percentage,
                        isSelected = isSelected,
                        onClick = { viewModel.votePlatform(platform.id) }
                    )
                }
            }

            HorizontalDivider(color = DarkGrey, modifier = Modifier.padding(vertical = 24.dp))

            // Section 2: Rate this movie
            SectionTitle("قيّم هذا الفيلم")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val ratings = listOf(
                    RatingItem("مذهل", "5"),
                    RatingItem("ممتاز", "4"),
                    RatingItem("جيد", "3"),
                    RatingItem("مقبول", "2"),
                    RatingItem("سيئ", "1")
                )
                ratings.forEach { rating ->
                    val isSelected = userVotes.rating == rating.id
                    val votes = stats.ratings[rating.id] ?: 0L
                    val total = stats.totalVotesRatings
                    val percentage = if (total > 0L) ((votes * 100L) / total).toInt() else 0

                    RatingCard(
                        rating = rating,
                        percentage = percentage,
                        isSelected = isSelected,
                        onClick = { viewModel.voteRating(rating.id) }
                    )
                }
            }

            HorizontalDivider(color = DarkGrey, modifier = Modifier.padding(vertical = 24.dp))

            // Section 3: Your feeling towards the episode?
            SectionTitle("شعورك تجاه الحلقة؟")
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = 4
            ) {
                val emotions = listOf(
                    EmotionItem("مصدوم", "shocked", "😵"),
                    EmotionItem("محبط", "disappointed", "😤"),
                    EmotionItem("حزين", "sad", "😭"),
                    EmotionItem("متأمل", "hopeful", "🤔"),
                    EmotionItem("متأثر", "touched", "🥺"),
                    EmotionItem("مستمتع", "enjoyed", "😆"),
                    EmotionItem("خائف", "scared", "😱"),
                    EmotionItem("ضجر", "bored", "😑"),
                    EmotionItem("مستوعب", "understanding", "😌"),
                    EmotionItem("مفتون", "fascinated", "🤩"),
                    EmotionItem("مرتبك", "confused", "🙃"),
                    EmotionItem("متوتر", "tense", "😬")
                )
                emotions.forEach { emotion ->
                    val isSelected = userVotes.emotion == emotion.id
                    val votes = stats.emotions[emotion.id] ?: 0L
                    val total = stats.totalVotesEmotions
                    val percentage = if (total > 0L) ((votes * 100L) / total).toInt() else 0

                    EmotionCard(
                        emotion = emotion,
                        percentage = percentage,
                        isSelected = isSelected,
                        onClick = { viewModel.voteEmotion(emotion.id) }
                    )
                }
            }

            if (item.credits?.cast?.isNotEmpty() == true) {
                HorizontalDivider(color = DarkGrey, modifier = Modifier.padding(vertical = 24.dp))

                // Section 4: Who was your favorite?
                SectionTitle("من كان المفضل لديك؟")
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(item.credits.cast.take(15)) { cast ->
                        val isSelected = userVotes.favorite_character == cast.id.toString()
                        val votes = stats.favorite_characters[cast.id.toString()] ?: 0L
                        val total = stats.totalVotesCharacters
                        val percentage = if (total > 0L) ((votes * 100L) / total).toInt() else 0

                        CharacterCard(
                            cast = cast,
                            percentage = percentage,
                            isSelected = isSelected,
                            onClick = { viewModel.voteCharacter(cast.id.toString()) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Floating Action Button
        Button(
            onClick = { /* TODO: Comments */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .width(200.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6EFD)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("٠ تعليق", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

data class PlatformItem(val name: String, val id: String, val emoji: String)

@Composable
fun PlatformCard(platform: PlatformItem, percentage: Int, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFF333333) else Color(0xFF1A1A1A))
                .border(2.dp, if (isSelected) GoldYellow else Color.Transparent, RoundedCornerShape(12.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = platform.emoji, fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = platform.name, color = TextPrimary, fontSize = 12.sp)
        Text(text = "%$percentage", color = TextSecondary, fontSize = 12.sp)
    }
}

data class RatingItem(val name: String, val id: String)

@Composable
fun RatingCard(rating: RatingItem, percentage: Int, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp).clickable { onClick() }
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = if (isSelected) GoldYellow else TextSecondary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = rating.name, color = TextPrimary, fontSize = 12.sp)
        Text(text = "%$percentage", color = TextSecondary, fontSize = 12.sp)
    }
}

data class EmotionItem(val name: String, val id: String, val emoji: String)

@Composable
fun EmotionCard(emotion: EmotionItem, percentage: Int, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp).width(70.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) GoldYellow else Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emotion.emoji, fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = emotion.name, color = if (isSelected) GoldYellow else TextPrimary, fontSize = 12.sp)
        Text(text = "%$percentage", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun CharacterCard(cast: com.example.data.remote.CastMember, percentage: Int, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, if (isSelected) GoldYellow else Color.Transparent, RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = cast.profile_path?.let { "https://image.tmdb.org/t/p/w185$it" },
                contentDescription = cast.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(DarkGrey)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cast.character.uppercase(),
            color = TextPrimary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold
        )
        Text(text = "%$percentage", color = TextSecondary, fontSize = 12.sp)
    }
}
