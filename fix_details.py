import sys
content = open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt").read()

content = content.replace("fun AboutTabContent(item: MediaItem, collectionDetails: com.example.data.remote.CollectionDetailsResponse?, onNavigateToDetails: (String, Int) -> Unit) {",
"fun AboutTabContent(item: MediaItem, collectionDetails: com.example.data.remote.CollectionDetailsResponse?, onNavigateToDetails: (String, Int) -> Unit, onNavigateToPerson: ((Int) -> Unit)? = null) {")

content = content.replace("AboutTabContent(item, uiState.collectionDetails, onNavigateToDetails)",
"AboutTabContent(item, uiState.collectionDetails, onNavigateToDetails, onNavigateToPerson)")

open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w").write(content)
