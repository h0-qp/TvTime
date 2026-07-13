import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

# Replace static genres
old_genres = 'val genres = "خيال, دراما, مغامرة, حركة, حربي" // Using a static one for now as TMDB genre list would require extra parsing'
new_genres = 'val genres = item.genres?.joinToString(", ") { it.name } ?: ""'
content = content.replace(old_genres, new_genres)

# Replace "الحاضر" which means present, but maybe it's just a movie
old_year_line = 'Text(text = "$year - الحاضر • $genres", color = TextSecondary, fontSize = 14.sp)'
new_year_line = 'Text(text = "$year • $genres", color = TextSecondary, fontSize = 14.sp)'
content = content.replace(old_year_line, new_year_line)

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

