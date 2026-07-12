with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'r') as f:
    content = f.read()

# Pass viewModel to screen so we can call toggleEpisodeWatched
content = content.replace("fun TvShowsScreen(viewModel: TvShowsViewModel, onNavigateToDetails: (String, Int) -> Unit) {", "fun TvShowsScreen(viewModel: TvShowsViewModel, onNavigateToDetails: (String, Int) -> Unit) {")

# Find the items(episodes) inside the Upcoming list
content = content.replace("UpcomingEpisodeCard(epData, onNavigateToDetails)", """UpcomingEpisodeCard(
                                            epData = epData, 
                                            onNavigateToDetails = onNavigateToDetails,
                                            onToggleWatched = { showId, epKey -> 
                                                viewModel.toggleEpisodeWatched(showId, epKey) 
                                            }
                                        )""")

# Update UpcomingEpisodeCard definition
new_card = """@Composable
fun UpcomingEpisodeCard(
    epData: UpcomingEpisodeData, 
    onNavigateToDetails: (String, Int) -> Unit,
    onToggleWatched: (Int, String) -> Unit
) {
    val seasonStr = epData.episodeToAir.season_number.toString().padStart(2, '0')
    val epStr = epData.episodeToAir.episode_number.toString().padStart(2, '0')
    val epKey = "S${epData.episodeToAir.season_number}E${epData.episodeToAir.episode_number}"
    val isWatched = epData.show.watchedEpisodes.contains(epKey)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(if (isWatched) Color(0xFF1E1E1E) else DarkGrey, RoundedCornerShape(12.dp))
            .clickable { onNavigateToDetails("tv", epData.show.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isWatched) Color(0xFF4CAF50) else Color.White, CircleShape)
                .clickable { onToggleWatched(epData.show.id, epKey) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Watched", tint = if (isWatched) Color.White else Color.LightGray)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Details
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            // Show name pill
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("< ${epData.show.title.uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S$seasonStr | E$epStr",
                color = if (isWatched) Color.Gray else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = epData.episodeToAir.name,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Image
        val imageUrl = epData.episodeToAir.still_path?.let { "https://image.tmdb.org/t/p/w500$it" } 
            ?: epData.showDetails.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
        
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isWatched) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
            }
        }
    }
}"""

import re
content = re.sub(r"@Composable\s*fun UpcomingEpisodeCard.*?\}\s*\}", new_card, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'w') as f:
    f.write(content)
