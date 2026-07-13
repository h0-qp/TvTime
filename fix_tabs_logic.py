import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

# Update tabs array
old_tabs_array = 'val tabs = if (mediaType == "tv") listOf("حول", "الحلقات") else listOf("حول")'
new_tabs_array = 'val tabs = if (mediaType == "tv") listOf("حول", "أكثر", "الحلقات") else listOf("حول", "أكثر")'
content = content.replace(old_tabs_array, new_tabs_array)

# Update if (selectedTab == 0) logic
old_logic = """                            if (selectedTab == 0) {
                                // About Tab
                                AboutTabContent(item = item)
                            } else {
                                // Episodes Tab
                                if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {"""

new_logic = """                            if (selectedTab == 0) {
                                // About Tab
                                AboutTabContent(item = item)
                            } else if (selectedTab == 1) {
                                // More Tab
                                MoreTabContent(item = item)
                            } else if (selectedTab == 2) {
                                // Episodes Tab
                                if (mediaType == "tv" && !item.seasons.isNullOrEmpty()) {"""
                                
content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

