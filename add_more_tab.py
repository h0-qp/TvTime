import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

more_tab_code = """
@Composable
fun MoreTabContent(item: MediaItem) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "مقاطع فيديو إضافية", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        val extras = item.videos?.results?.filter { it.type != "Trailer" }
        if (!extras.isNullOrEmpty()) {
            extras.forEach { video ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(8.dp)).background(DarkGrey).clickable { },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                        Text(text = video.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = video.type, color = TextSecondary, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier.size(80.dp).background(Color(0xFF333333)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }
                }
            }
        } else {
            Text(text = "لا توجد مقاطع إضافية.", color = TextSecondary, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // You could add production companies here if we parsed them
    }
}
"""

if "fun MoreTabContent" not in content:
    content = content + "\n" + more_tab_code
    with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
        f.write(content)

