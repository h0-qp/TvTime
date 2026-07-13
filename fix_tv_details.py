import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

# 1. Update duration string
old_duration = """                        val hours = (item.runtime ?: 0) / 60
                        val mins = (item.runtime ?: 0) % 60
                        val durationStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m\""""

new_duration = """                        val durationStr = if (mediaType == "tv") {
                            val seasonCount = item.number_of_seasons ?: item.seasons?.count { it.season_number > 0 } ?: 0
                            "$seasonCount موسم/مواسم"
                        } else {
                            val hours = (item.runtime ?: 0) / 60
                            val mins = (item.runtime ?: 0) % 60
                            if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        }"""

content = content.replace(old_duration, new_duration)

# 2. Update tabs list
old_tabs = """                            val tabs = if (mediaType == "tv") listOf("حول", "أكثر", "الحلقات") else listOf("حول", "أكثر")"""
new_tabs = """                            val tabs = if (mediaType == "tv") listOf("حول", "الحلقات") else listOf("حول", "أكثر")"""

content = content.replace(old_tabs, new_tabs)

# 3. Update tab rendering
old_tab_content = """                            if (selectedTab == 0) {
                                // About Tab
                                AboutTabContent(item = item)
                            } else if (selectedTab == 1) {
                                // More Tab
                                MoreTabContent(item = item)
                            } else if (selectedTab == 2) {
                                // Episodes Tab
                                if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {"""

new_tab_content = """                            if (selectedTab == 0) {
                                // About Tab
                                AboutTabContent(item = item)
                            } else if (selectedTab == 1 && mediaType != "tv") {
                                // More Tab (Movies)
                                MoreTabContent(item = item)
                            } else if ((selectedTab == 1 && mediaType == "tv") || selectedTab == 2) {
                                // Episodes Tab
                                if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {"""

content = content.replace(old_tab_content, new_tab_content)

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

