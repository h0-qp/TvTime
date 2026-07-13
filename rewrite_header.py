import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

start_marker = "                        // Backdrop"
end_marker = "                            Spacer(modifier = Modifier.height(24.dp))"
start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

# We will replace from start_idx to end_idx with our new layout
new_layout = """                        val hours = (item.runtime ?: 0) / 60
                        val mins = (item.runtime ?: 0) % 60
                        val durationStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
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
                                    Icon(androidx.compose.material.icons.Icons.Default.MoreHoriz, contentDescription = "Options", tint = TrueBlack, modifier = Modifier.background(Color.White.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape).padding(4.dp))
                                }
                                IconButton(onClick = { onNavigateBack() }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowDown, contentDescription = "Back", tint = TrueBlack, modifier = Modifier.background(Color.White.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape).padding(4.dp))
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
                        val isWatched = state.isInWatchlist // Reusing watchlist for watched state based on prompt
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Side (Right in LTR code, but since we use SpaceBetween, it will lay out based on layout direction. We'll manually order them or rely on Compose RTL support)
                            // We will place them in LTR order: Checkmark (left), Eye (middle), Date (right). Wait, RTL layout places first item on the right.
                            // The screenshot shows Date on the right. So Date is the first item in the Row.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = "Date", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = date, color = TextSecondary, fontSize = 14.sp)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(androidx.compose.material.icons.Icons.Outlined.Visibility, contentDescription = "Visibility", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isWatched) "تمت المشاهدة" else "لم يُشاهد", color = TextSecondary, fontSize = 14.sp)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (isWatched) GoldYellow else TrueBlack)
                                    .border(1.dp, if (isWatched) GoldYellow else TextSecondary, androidx.compose.foundation.shape.CircleShape)
                                    .clickable { viewModel.toggleWatchlist() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = "Watched",
                                    tint = if (isWatched) TrueBlack else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))"""

content = content[:start_idx] + new_layout + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

