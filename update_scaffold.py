import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

# Find the Scaffold block
scaffold_start = content.find("    Scaffold(")
scaffold_end = content.find("    ) { innerPadding ->", scaffold_start)

new_scaffold = """    Scaffold(
        containerColor = TrueBlack"""

content = content[:scaffold_start] + new_scaffold + content[scaffold_end:]

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

