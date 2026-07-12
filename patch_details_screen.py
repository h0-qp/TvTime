import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# Replace the info column with the tabs
old_info_section = """                            // Seasons & Episodes
                            if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {
                                Text(
                                    text = "المواسم",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
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
                                    Text(
                                        text = "الحلقات",
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
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
                                                    text = episode.overview.take(100).let { if (it.length == 100) "$it..." else it }.ifEmpty { "لا توجد قصة للحلقة." },
                                                    color = TextSecondary,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                            }"""

new_info_section = """
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
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = { Text(text = title, fontWeight = FontWeight.Bold, color = if (selectedTab == index) TextPrimary else TextSecondary) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            if (selectedTab == 0) {
                                // About Tab
                                AboutTabContent(item = item)
                            } else {
                                // Episodes Tab
""" + old_info_section + """
                            }
"""

if old_info_section in content:
    content = content.replace(old_info_section, new_info_section)
else:
    print("Could not find old_info_section")

# Add AboutTabContent function
about_tab_code = """
@Composable
fun AboutTabContent(item: com.example.data.remote.MediaItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // "أين تُشاهد" section
        Text(text = "أين تُشاهد", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(text = "OSN+", color = TextPrimary, fontWeight = FontWeight.Bold)
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
        val genres = item.genres?.joinToString(", ") { it.name } ?: "دراما, مغامرة"
        Text(text = "$year - الحاضر • $genres", color = TextSecondary, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(8.dp))
        val rating = item.vote_average?.let { String.format("%.1f", it) } ?: "0.0"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$rating/5", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            repeat(5) {
                Icon(Icons.Default.Check, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(14.dp)) // Using Check as placeholder for star
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = item.overview.ifEmpty { "لا توجد قصة متاحة." }, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val trailer = item.videos?.results?.firstOrNull { it.type == "Trailer" }
        if (trailer != null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Play Trailer", tint = TextPrimary, modifier = Modifier.size(48.dp)) // Placeholder for Play button
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
                items(item.credits!!.cast.take(10)) { actor ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w185${actor.profile_path}",
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
        }
        
        // Similar
        if (!item.similar?.results.isNullOrEmpty()) {
            Text(text = "ما شاهده الناس أيضًا", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(item.similar!!.results.take(10)) { similarItem ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${similarItem.poster_path}",
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
"""

if "fun AboutTabContent" not in content:
    content = content + "\n" + about_tab_code

# Add necessary imports
imports = """
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableIntStateOf
"""
if "import androidx.compose.material3.TabRow" not in content:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\n" + imports)


# We should remove the old overview display from the top since it's now in the 'About' tab.
# Let's remove the old القصة text
old_overview = """                            Text(
                                text = "القصة",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.overview.ifEmpty { "لا توجد قصة متاحة." },
                                color = TextSecondary,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))"""

if old_overview in content:
    content = content.replace(old_overview, "")
else:
    print("Could not find old overview to remove")


with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)
    
