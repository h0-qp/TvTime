import re

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''            // Title Badge
            Box(
                modifier = Modifier
                    .border(1.dp, TextSecondary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "< ${(show.name ?: show.title ?: "").uppercase()}",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("82+ S01 | E04", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(GoldYellow, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("جديد", color = TrueBlack, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = show.overview.ifEmpty { "Episode description placeholder..." },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${show.poster_path}",''',
'''            // Title Badge
            Box(
                modifier = Modifier
                    .border(1.dp, TextSecondary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = show.title.uppercase(),
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("تمت المشاهدة: ${show.watchedEpisodes.size} حلقة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${show.posterPath}",'''
)

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'w') as f:
    f.write(content)

