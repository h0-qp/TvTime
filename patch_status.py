import re

file_path = "app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                        val durationStr = if (mediaType == "tv") {
                            val seasonCount = item.number_of_seasons ?: item.seasons?.count { it.season_number > 0 } ?: 0
                            "$seasonCount موسم/مواسم"
                        } else {"""

replacement = """                        val durationStr = if (mediaType == "tv") {
                            val seasonCount = item.number_of_seasons ?: item.seasons?.count { it.season_number > 0 } ?: 0
                            val statusSuffix = if (item.status == "Ended" || item.status == "Canceled") " • منتهي" else ""
                            "$seasonCount موسم/مواسم$statusSuffix"
                        } else {"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
