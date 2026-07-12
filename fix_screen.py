with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'r') as f:
    content = f.read()

# We need to remove the text starting from the trailing spacer
bad_part = """                Spacer(modifier = Modifier.width(16.dp))
                
                // Image
        val imageUrl = epData.episodeToAir.still_path?.let { "https://image.tmdb.org/t/p/w500$it" } 
            ?: epData.showDetails.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
        
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )
    }
}"""

content = content.replace(bad_part, "")

with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsScreen.kt', 'w') as f:
    f.write(content)
