import re

file_path = "app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# Add rememberLazyListState to imports if missing
if "import androidx.compose.foundation.lazy.rememberLazyListState" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.LazyRow\n", "import androidx.compose.foundation.lazy.LazyRow\nimport androidx.compose.foundation.lazy.rememberLazyListState\n")

# Hoist seasonsListState
if "val seasonsListState = rememberLazyListState()" not in content:
    content = content.replace("val detailsScrollState = rememberScrollState()", "val detailsScrollState = rememberScrollState()\n    val seasonsListState = rememberLazyListState()")

# Update LazyRow for seasons
content = content.replace("LazyRow(\n                                        horizontalArrangement = Arrangement.spacedBy(8.dp)\n                                    ) {", "LazyRow(\n                                        state = seasonsListState,\n                                        horizontalArrangement = Arrangement.spacedBy(8.dp)\n                                    ) {")

with open(file_path, "w") as f:
    f.write(content)
