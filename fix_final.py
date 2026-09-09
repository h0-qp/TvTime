import re

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'r') as f:
    content = f.read()

# I see it still says `showComments` in two places: line 896 and 1199.
# This means my previous script didn't catch them or they were restored.
# Let's fix them to `onCommentsClick()` if they are in EpisodeDetailsContent or AboutTabContent, OR if we are inside DetailsScreen we just define `var showComments by remember { mutableStateOf(false) }`.
# Wait, DetailsScreen ALREADY has `var showComments`. But the error is "Unresolved reference 'showComments'".
# This means those lines are OUTSIDE the DetailsScreen composable!

# Where is line 896?
# Ah! EpisodeDetailsContent and AboutTabContent are SEPARATE COMPOSABLES!
# If we want them to show comments, they need to call the callback `onCommentsClick()`.
content = content.replace("showComments = true", "onCommentsClick()")

# But what about the one INSIDE DetailsScreen that actually toggles the state?
# The state is defined in DetailsScreen.
# Wait, if I replace ALL `showComments = true` with `onCommentsClick()`, then how does DetailsScreen show it?
# In DetailsScreen, we passed `onCommentsClick = { showComments = true }` to EpisodeDetailsContent.
# But what about the `showComments` inside DetailsScreen itself?

# Let's just fix it by replacing `.clickable { showComments = true }` with `.clickable { onCommentsClick() }` inside EpisodeDetailsContent, and AboutTabContent.
# But if it's already `onCommentsClick()`, why did it fail? Because I previously replaced `onCommentsClick()` WITH `showComments = true` globally in `fix_final_comments_click.py`!

# So let's restore `.clickable { onCommentsClick() }` globally, except in DetailsScreen we need the state.
content = content.replace(".clickable { showComments = true }", ".clickable { onCommentsClick() }")

# Now, we need to make sure DetailsScreen has the state `showComments` and passes it.
# Actually, wait. The 'التعليقات' (Comments) button is INSIDE `AboutTabContent`! And inside `EpisodeDetailsContent`!
# Let's see if AboutTabContent takes `onCommentsClick`. It doesn't!
# `fun AboutTabContent(item: MediaItem, collectionDetails: com.example.data.remote.CollectionDetailsResponse?, onNavigateToDetails: (String, Int) -> Unit, onNavigateToPerson: ((Int) -> Unit)? = null)`
# We need to add `onCommentsClick: () -> Unit` to AboutTabContent!

if "fun AboutTabContent(" in content and "onCommentsClick" not in content.split("fun AboutTabContent(")[1].split(")")[0]:
    content = content.replace("fun AboutTabContent(\n    item: MediaItem,", "fun AboutTabContent(\n    item: MediaItem,\n    onCommentsClick: () -> Unit,")
    content = content.replace("fun AboutTabContent(item: MediaItem, collectionDetails", "fun AboutTabContent(item: MediaItem, onCommentsClick: () -> Unit, collectionDetails")
    
    # Update calls to AboutTabContent inside DetailsScreen
    content = content.replace("AboutTabContent(\n                                item = state.mediaItem", "AboutTabContent(\n                                item = state.mediaItem,\n                                onCommentsClick = { showComments = true }")
    content = content.replace("AboutTabContent(item = state.mediaItem, collectionDetails", "AboutTabContent(item = state.mediaItem, onCommentsClick = { showComments = true }, collectionDetails")
    
# Update calls to EpisodeDetailsContent inside DetailsScreen
# We already did this, but let's make sure it's correct.
content = content.replace("onCommentsClick = { onCommentsClick() }", "onCommentsClick = { showComments = true }")

with open('app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt', 'w') as f:
    f.write(content)

