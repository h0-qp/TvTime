#!/bin/bash
sed -i 's/val uiState by viewModel.uiState.collectAsState()/val uiState by viewModel.uiState.collectAsState()\n    var showBottomSheet by remember { mutableStateOf(false) }\n    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)/g' app/src/main/java/com/example/ui/screens/details/DetailsScreen.kt
