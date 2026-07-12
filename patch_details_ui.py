import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp''',
'''import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp'''
)

content = content.replace(
'''                            Text(
                                text = item.overview.ifEmpty { "لا توجد قصة متاحة." },
                                color = TextSecondary,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }''',
'''                            Text(
                                text = item.overview.ifEmpty { "لا توجد قصة متاحة." },
                                color = TextSecondary,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Seasons & Episodes
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
                            }
                        }
                    }
                }
            }'''
)

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)

