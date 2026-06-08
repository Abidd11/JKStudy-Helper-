package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyMaterial
import com.example.ui.viewmodel.StudyViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMaterialsScreen(
    categoryCode: String,
    viewModel: StudyViewModel,
    onBackClick: () -> Unit,
    onOpenPdfReader: (String) -> Unit
) {
    val context = LocalContext.current
    val materialsState by viewModel.materialsState.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    val downloadingId by viewModel.downloadingFileId.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadNotification by viewModel.downloadNotification.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Display appropriate screen header titles
    val displayTitle = remember(categoryCode) {
        when (categoryCode.lowercase()) {
            "class8" -> "Class 8 Materials"
            "class9" -> "Class 9 Materials"
            "class10" -> "Class 10 Hub"
            "class11" -> "Class 11 Hub"
            "class12" -> "Class 12 Hub"
            "jkbose" -> "JKBOSE Boards"
            "cbse" -> "CBSE Boards"
            "neet" -> "NEET Aspirants"
            "jee" -> "JEE Main & Advanced"
            "cuet" -> "CUET Syllabus"
            "ssc" -> "SSC Prep"
            "upsc" -> "UPSC Prep"
            "generalknowledge" -> "General Knowledge"
            "currentaffairs" -> "Current Affairs"
            else -> "Study materials"
        }
    }

    // Filter sub-tags for Study Materials Types
    val filterTypes = listOf(
        "All", "Notes", "PYQs", "Guess Papers", "Important Questions",
        "Sample Papers", "Books", "Chapter Wise Notes", "MCQs",
        "Practical Files", "Assignments"
    )

    // Trigger toast notification on download state changes
    LaunchedEffect(downloadNotification) {
        downloadNotification?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearNotification()
        }
    }

    // Match materials according to search text and filters
    val filteredMaterials = remember(materialsState, searchQuery, selectedTypeFilter, categoryCode) {
        val allMaterials = if (materialsState is com.example.data.repository.StudyState.Success) {
            (materialsState as com.example.data.repository.StudyState.Success).materials
        } else {
            viewModel.getMaterialsFromCache()
        }
        allMaterials.filter { material ->
            val matchesCategory = material.matchesCategory(categoryCode)
            val matchesQuery = material.fileName.lowercase().contains(searchQuery.lowercase())
            val matchesFilter = if (selectedTypeFilter == "All") {
                true
            } else {
                material.matchesMaterialType(selectedTypeFilter)
            }
            matchesCategory && matchesQuery && matchesFilter
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Text(
                    "Filter Study Materials",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Select a material type to filter files in $displayTitle:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(modifier = Modifier.heightIn(max = 280.dp)) {
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterTypes.size) { index ->
                                val type = filterTypes[index]
                                val isSelected = selectedTypeFilter == type
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTypeFilter = type
                                            showFilterDialog = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = when (type) {
                                                "All" -> Icons.Default.School
                                                "Notes" -> Icons.Default.Book
                                                "PYQs" -> Icons.Default.Help
                                                "Guess Papers" -> Icons.Default.Assignment
                                                "Important Questions" -> Icons.Default.Star
                                                "Sample Papers" -> Icons.Default.InsertDriveFile
                                                "Books" -> Icons.Default.MenuBook
                                                "Chapter Wise Notes" -> Icons.Default.Folder
                                                "MCQs" -> Icons.Default.List
                                                "Practical Files" -> Icons.Default.InsertDriveFile
                                                "Assignments" -> Icons.Default.Assignment
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = type,
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = if (selectedTypeFilter != "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedTypeFilter != "All") {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            com.example.ui.ads.UnityBannerAd(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                placementId = "Banner_Android"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // New dedicated Search Bar below TopAppBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search inside $displayTitle...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
            if (selectedTypeFilter != "All") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = { selectedTypeFilter = "All" },
                        label = { Text("Filter: $selectedTypeFilter", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Filter",
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // List of downloadable PDF files
            if (filteredMaterials.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No study files found here yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing filter categories or check back later for updates.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMaterials) { material ->
                        // Check if file is already downloaded/exists locally
                        val localDownload = downloads.find { it.fileId == material.fileId }
                        val isDownloaded = localDownload != null
                        val isDownloading = downloadingId == material.fileId

                        val favoriteFlow = viewModel.isFavorite(material.fileId)
                        val isFavoriteState by favoriteFlow.collectAsState(initial = false)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Tag indicator
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = material.inferMaterialTypeString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Action bar icons (Favorite marker)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleFavorite(material.fileId)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isFavoriteState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Toggle favorite",
                                                tint = if (isFavoriteState) Color.Red else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (isDownloaded) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                                        shape = CircleShape
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "Downloaded",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // FileName with dynamic limit wrapping
                                Text(
                                    text = material.fileName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // File meta items row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column {
                                        Text("File Size", fontSize = 9.sp, color = Color.Gray)
                                        Text("${material.fileSize} MB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Release Date", fontSize = 9.sp, color = Color.Gray)
                                        Text(material.dateString, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Curriculum", fontSize = 9.sp, color = Color.Gray)
                                        Text(material.inferCategoryString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons (Open or One Click Download)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    when {
                                        isDownloading -> {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "Saving to app storage paths...",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "${(downloadProgress * 100).toInt()}%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                LinearProgressIndicator(
                                                    progress = { downloadProgress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(CircleShape)
                                                )
                                            }
                                        }
                                        isDownloaded -> {
                                            Button(
                                                onClick = {
                                                    viewModel.triggerInterstitial {
                                                        // Move into reader & record index view
                                                        viewModel.trackRecentView(material.fileId)
                                                        onOpenPdfReader(material.fileId)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Open in Built-In Reader", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        else -> {
                                            ElevatedButton(
                                                onClick = {
                                                    viewModel.triggerDownload(material) { downloadEntity ->
                                                        // Auto Open automatically after downloading completed!
                                                        viewModel.trackRecentView(material.fileId)
                                                        onOpenPdfReader(material.fileId)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.elevatedButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Icon(Icons.Default.DownloadForOffline, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("One Click Download", fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
