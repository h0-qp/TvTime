import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

end_marker = "    }\n}\n\n@Composable\nfun EpisodeDetailsContent("
start_idx = content.find("        Spacer(modifier = Modifier.height(32.dp))\n    }\n}\n\n@Composable\nfun EpisodeDetailsContent(")

if start_idx != -1:
    new_content = """        if (item.credits?.cast?.isNotEmpty() == true) {
            Text(text = "طاقم الممثلين", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(item.credits.cast.take(10)) { cast ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                        AsyncImage(
                            model = cast.profile_path?.let { "https://image.tmdb.org/t/p/w185$it" },
                            contentDescription = cast.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape).background(DarkGrey)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = cast.name, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(text = cast.character, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (item.similar?.results?.isNotEmpty() == true) {
            Text(text = "ما شاهده الناس أيضاً", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(item.similar.results.take(10)) { similarItem ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${similarItem.poster_path}",
                        contentDescription = similarItem.title ?: similarItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EpisodeDetailsContent("""
    
    content = content.replace("        Spacer(modifier = Modifier.height(32.dp))\n    }\n}\n\n@Composable\nfun EpisodeDetailsContent(", new_content)

    with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
        f.write(content)
else:
    print("Could not find marker")

