import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

# Add comments section after Similar list
comments_code = """
        Text(text = "التعليقات", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "لا توجد تعليقات متاحة حالياً.", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
"""

if "التعليقات" not in content:
    content = content.replace("    }\n}\n\n@Composable\nfun EpisodeDetailsContent(", comments_code + "    }\n}\n\n@Composable\nfun EpisodeDetailsContent(")
    with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
        f.write(content)

