import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

old_box = """                                    .background(if (isWatched) GoldYellow else TrueBlack)
                                    .border(1.dp, if (isWatched) GoldYellow else TextSecondary, androidx.compose.foundation.shape.CircleShape)"""
new_box = """                                    .background(if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TrueBlack)
                                    .border(1.dp, if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TextSecondary, androidx.compose.foundation.shape.CircleShape)"""

content = content.replace(old_box, new_box)

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

