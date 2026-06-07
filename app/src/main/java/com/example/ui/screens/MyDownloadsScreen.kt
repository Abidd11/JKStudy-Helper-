package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.data.model.StudyMaterial
import com.example.ui.viewmodel.StudyViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDownloadsScreen(
    viewModel: StudyViewModel,
    onBackClick: () -> Unit,
    onOpenPdfReader: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTabState by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Downloads", "Favorite List")

    val downloads by viewModel.downloads.collectAsState()
    val favoriteMaterials by viewModel.favoriteMaterials.collectAsState()

    var showDeleteDialogForId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Library", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Selection Row
            TabRow(
                selectedTabIndex = selectedTabState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabState == index,
                        onClick = { selectedTabState = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabState == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTabState) {
                0 -> {
                    // Downloads Cabinet Screen list
                    if (downloads.isEmpty()) {
                        EmptyLibraryState(
                            icon = Icons.Default.DownloadForOffline,
                            header = "No files saved locally yet",
                            subtitle = "Click on Category cards, select your syllabus notes & trigger 'One Click Download' to save them and learn offline anytime."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(downloads) { download ->
                                DownloadFileRow(
                                    download = download,
                                    onOpen = {
                                        viewModel.trackRecentView(download.fileId)
                                        onOpenPdfReader(download.fileId)
                                    },
                                    onShare = {
                                        // Standard Sharing Action
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "JK Study Helper - Sharing PDF Books Notes:\nFile: ${download.fileName}\nSize: ${download.fileSize} MB\nOffline Path: ${download.localPath}\nStudy anywhere with JK Study Helper!"
                                            )
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share Study File")
                                        context.startActivity(shareIntent)
                                    },
                                    onDelete = {
                                        showDeleteDialogForId = download.fileId
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Favorites Screen list showing Bookmarked flat items
                    if (favoriteMaterials.isEmpty()) {
                        EmptyLibraryState(
                            icon = Icons.Default.FavoriteBorder,
                            header = "Favorite tray empty",
                            subtitle = "Show love to helpful PDFs! Mark heart badges on any note card across the category browser to access them instantly from here."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoriteMaterials) { material ->
                                val localDownload = downloads.find { it.fileId == material.fileId }
                                val isDownloaded = localDownload != null

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            if (isDownloaded) {
                                                viewModel.trackRecentView(material.fileId)
                                                onOpenPdfReader(material.fileId)
                                            } else {
                                                // Trigger fast downloads
                                                viewModel.triggerDownload(material) {
                                                    viewModel.trackRecentView(material.fileId)
                                                    onOpenPdfReader(material.fileId)
                                                }
                                            }
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = material.fileName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = material.inferMaterialTypeString(),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "•  ${material.fileSize} MB",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(onClick = { viewModel.toggleFavorite(material.fileId) }) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Unfavorite", tint = Color.Red)
                                        }
                                        Icon(
                                            imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.DownloadForOffline,
                                            contentDescription = null,
                                            tint = if (isDownloaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Deletion confirmation dialog
    if (showDeleteDialogForId != null) {
        val targetId = showDeleteDialogForId!!
        val fileItem = downloads.find { it.fileId == targetId }
        AlertDialog(
            onDismissRequest = { showDeleteDialogForId = null },
            title = { Text("Delete Local PDF Copy?") },
            text = {
                Text(
                    "This action permanently deletes the downloaded PDF file for \"${fileItem?.fileName ?: ""}\" from local application directories. You will need to re-download it to open it in offline mode.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDownloadedFile(targetId)
                        showDeleteDialogForId = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogForId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DownloadFileRow(
    download: DownloadEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(download.timestamp) {
        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        formatter.format(Date(download.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.fileName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Information row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Size", fontSize = 9.sp, color = Color.Gray)
                        Text("${download.fileSize} MB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Downloaded", fontSize = 9.sp, color = Color.Gray)
                        Text(dateString, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bookmark bookmarks indicator
                if (download.bookmarkPages.isNotEmpty()) {
                    val count = download.bookmarkPages.split(",").size
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$count Bookmarks", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action triggers panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Copy", tint = MaterialTheme.colorScheme.error)
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    header: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = header,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
