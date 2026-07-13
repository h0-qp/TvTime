#!/bin/bash
sed -i 's/var isAdded by remember { mutableStateOf(false) }/val isAdded = currentState.watchlistIds.contains(item.id)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/clickable { isAdded = !isAdded }/clickable { onToggleAdd(item) }/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/fun DiscoverPosterCard(item: MediaItem, onNavigateToDetails: (String, Int) -> Unit)/fun DiscoverPosterCard(item: MediaItem, currentState: ExploreUiState.Success, onToggleAdd: (MediaItem) -> Unit, onNavigateToDetails: (String, Int) -> Unit)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/fun FeedCard(item: MediaItem, onNavigateToDetails: (String, Int) -> Unit)/fun FeedCard(item: MediaItem, currentState: ExploreUiState.Success, onToggleAdd: (MediaItem) -> Unit, onNavigateToDetails: (String, Int) -> Unit)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/fun ActivityCard(item: MediaItem, onNavigateToDetails: (String, Int) -> Unit)/fun ActivityCard(item: MediaItem, currentState: ExploreUiState.Success, onToggleAdd: (MediaItem) -> Unit, onNavigateToDetails: (String, Int) -> Unit)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/DiscoverPosterCard(item = item, onNavigateToDetails = onNavigateToDetails)/DiscoverPosterCard(item = item, currentState = currentState, onToggleAdd = { viewModel.toggleWatchlist(it) }, onNavigateToDetails = onNavigateToDetails)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/FeedCard(item = item, onNavigateToDetails = onNavigateToDetails)/FeedCard(item = item, currentState = currentState, onToggleAdd = { viewModel.toggleWatchlist(it) }, onNavigateToDetails = onNavigateToDetails)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

sed -i 's/ActivityCard(item = item, onNavigateToDetails = onNavigateToDetails)/ActivityCard(item = item, currentState = currentState, onToggleAdd = { viewModel.toggleWatchlist(it) }, onNavigateToDetails = onNavigateToDetails)/g' app/src/main/java/com/example/ui/screens/explore/ExploreScreen.kt

# Also need to fix the SearchResultItem, if it has a + icon. Let's see if it does.
