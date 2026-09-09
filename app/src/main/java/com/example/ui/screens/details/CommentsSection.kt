package com.example.ui.screens.details

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.firebase.Comment
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrueBlack
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentsScreenFullScreen(
    comments: List<Comment>,
    onClose: () -> Unit,
    onAddComment: (text: String, imageUri: Uri?, isGif: Boolean) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            containerColor = TrueBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.clickable { onClose() }
                    )
                    Text(
                        text = "التعليقات",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp),
                    onClick = { showAddDialog = true },
                    containerColor = Color.White,
                    contentColor = TrueBlack,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Comment")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding() + 80.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(comments) { comment ->
                    CommentItem(comment = comment)
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddCommentFullScreenDialog(
            onClose = { showAddDialog = false },
            onPublish = { text, uri, isGif ->
                onAddComment(text, uri, isGif)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp).alpha(0f)) // Placeholder for menu
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                val dateStr = dateFormat.format(Date(comment.timestamp))
                Text(text = dateStr, color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = comment.userName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = comment.userProfileImage ?: "https://ui-avatars.com/api/?name=${comment.userName}",
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkGrey)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = comment.text,
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.End)
        )
        
        if (comment.imageUrl != null) {
            Spacer(modifier = Modifier.height(12.dp))
            AsyncImage(
                model = comment.imageUrl,
                contentDescription = "Attached Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Reply, contentDescription = "Reply", tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "0", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "0", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentFullScreenDialog(
    onClose: () -> Unit,
    onPublish: (String, Uri?, Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            containerColor = TrueBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkGrey)
                            .clickable {
                                if (text.isNotBlank() || selectedImageUri != null) {
                                    onPublish(text, selectedImageUri, false)
                                    onClose()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("نشر", color = TextPrimary, fontSize = 14.sp)
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkGrey)
                            .clickable { onClose() }
                            .padding(8.dp)
                    )
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = DarkGrey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gif,
                            contentDescription = "GIF",
                            tint = TextSecondary,
                            modifier = Modifier.size(32.dp).clickable { /* TODO: GIF picker */ }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp).clickable {
                                imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding() + 80.dp)
                    .padding(16.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("...شارك رأيك", color = TextSecondary, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End, fontSize = 18.sp)
                )
                
                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedImageUri = null }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
