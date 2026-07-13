import sys

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "r") as f:
    content = f.read()

# 1. Update imports
if "import kotlinx.coroutines.delay" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.delay")

# 2. Add showAddSuccess and bottom bar
old_scaffold_start = """    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = TrueBlack
    ) { innerPadding ->"""

new_scaffold_start = """    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var showAddSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(showAddSuccess) {
        if (showAddSuccess) {
            delay(2000)
            showAddSuccess = false
        }
    }

    Scaffold(
        containerColor = TrueBlack,
        bottomBar = {
            if (uiState is DetailsUiState.Success) {
                val state = uiState as DetailsUiState.Success
                if (state.selectedEpisodeDetails == null) {
                    val shouldShow = !state.isInWatchlist || showAddSuccess
                    if (shouldShow) {
                        val isSuccessState = state.isInWatchlist && showAddSuccess
                        val backgroundColor = if (isSuccessState) TrueBlack else GoldYellow
                        val textColor = if (isSuccessState) GoldYellow else TrueBlack
                        val icon = if (isSuccessState) Icons.Default.Check else Icons.Default.Add
                        val textStr = if (isSuccessState) "تمت الإضافة" else (if (mediaType == "tv") "إضافة مسلسل" else "إضافة فيلم")
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .clickable { 
                                    if (!state.isInWatchlist) {
                                        viewModel.toggleWatchlist()
                                        showAddSuccess = true
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = textColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(textStr, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->"""

content = content.replace(old_scaffold_start, new_scaffold_start)

# 3. Update Actions Row to only show Watched for movies
old_actions_row = """                        // Actions Row
                        val isWatched = state.isInWatchlist // Reusing watchlist for watched state based on prompt
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Side (Right in LTR code, but since we use SpaceBetween, it will lay out based on layout direction. We'll manually order them or rely on Compose RTL support)
                            // We will place them in LTR order: Checkmark (left), Eye (middle), Date (right). Wait, RTL layout places first item on the right.
                            // The screenshot shows Date on the right. So Date is the first item in the Row.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = "Date", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = date, color = TextSecondary, fontSize = 14.sp)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Visibility, contentDescription = "Visibility", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isWatched) "تمت المشاهدة" else "لم يُشاهد", color = TextSecondary, fontSize = 14.sp)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TrueBlack)
                                    .border(1.dp, if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TextSecondary, androidx.compose.foundation.shape.CircleShape)
                                    .clickable { viewModel.toggleWatchlist() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Watched",
                                    tint = if (isWatched) TrueBlack else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }"""

new_actions_row = """                        // Actions Row
                        val isWatched = state.firestoreItem?.isWatched == true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = "Date", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = date, color = TextSecondary, fontSize = 14.sp)
                            }
                            
                            if (mediaType == "movie") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Visibility, contentDescription = "Visibility", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isWatched) "تمت المشاهدة" else "لم يُشاهد", color = TextSecondary, fontSize = 14.sp)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TrueBlack)
                                        .border(1.dp, if (isWatched) androidx.compose.ui.graphics.Color(0xFF4CAF50) else TextSecondary, androidx.compose.foundation.shape.CircleShape)
                                        .clickable { 
                                            if (state.isInWatchlist) {
                                                viewModel.toggleMovieWatched() 
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Watched",
                                        tint = if (isWatched) TrueBlack else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }"""

content = content.replace(old_actions_row, new_actions_row)

with open("app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt", "w") as f:
    f.write(content)

