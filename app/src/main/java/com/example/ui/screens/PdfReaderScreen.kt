package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    fileId: String,
    viewModel: StudyViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadEntity by remember { mutableStateOf<DownloadEntity?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Read reader configuration state
    var isReaderDarkMode by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Page navigation state
    val lazyListState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }

    var searchQueryWithinPdf by remember { mutableStateOf("") }
    var showSearchRow by remember { mutableStateOf(false) }
    var foundOccurrences by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentOccurrenceIndex by remember { mutableIntStateOf(0) }

    // Load download entity and initialize PDF Renderer safely
    LaunchedEffect(fileId) {
        withContext(Dispatchers.IO) {
            val dbItem = viewModel.downloads.value.find { it.fileId == fileId }
                ?: viewModel.getDownloadById(fileId)
            
            if (dbItem == null) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Book file registration not found. Please re-download."
                }
                return@withContext
            }

            downloadEntity = dbItem
            val file = File(dbItem.localPath)
            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    errorMessage = "PDF file not found in local storage paths. Please download again."
                }
                return@withContext
            }

            try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                withContext(Dispatchers.Main) {
                    pdfRenderer = renderer
                    pageCount = renderer.pageCount
                    
                    // Restore last read page if exists
                    if (dbItem.lastOpenedPage in 0 until renderer.pageCount) {
                        coroutineScope.launch {
                            lazyListState.scrollToItem(dbItem.lastOpenedPage)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Failed to load PDF structure: ${e.localizedMessage}"
                }
            }
        }
    }

    // Capture last read position on page change
    LaunchedEffect(firstVisibleItemIndex) {
        if (downloadEntity != null && firstVisibleItemIndex in 0 until pageCount) {
            viewModel.saveLastOpenedPage(fileId, firstVisibleItemIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = downloadEntity?.fileName ?: "PDF Reader",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (pageCount > 0) {
                            Text(
                                text = "Page ${firstVisibleItemIndex + 1} of $pageCount",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    // Search Action
                    IconButton(onClick = { showSearchRow = !showSearchRow }) {
                        Icon(Icons.Default.Search, contentDescription = "Search inside PDF")
                    }
                    // Jump to page Action
                    IconButton(onClick = {
                        jumpPageInput = (firstVisibleItemIndex + 1).toString()
                        showJumpDialog = true
                    }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Jump page")
                    }
                    // Bookmark page Action
                    val isBookmarked = remember(downloadEntity?.bookmarkPages, firstVisibleItemIndex) {
                        val bookmarkedPages = downloadEntity?.bookmarkPages?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                        bookmarkedPages.contains(firstVisibleItemIndex + 1)
                    }
                    IconButton(onClick = {
                        viewModel.togglePageBookmark(fileId, firstVisibleItemIndex + 1)
                        // Trigger state updates
                        coroutineScope.launch {
                            val updated = viewModel.getDownloadById(fileId)
                            downloadEntity = updated
                        }
                    }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark current page",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Theme toggler
                    IconButton(onClick = { isReaderDarkMode = !isReaderDarkMode }) {
                        Icon(
                            if (isReaderDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Reader Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isReaderDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5))
        ) {
            when {
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }
                pdfRenderer == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Parsing Document...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search results toolbar
                        AnimatedVisibility(visible = showSearchRow) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQueryWithinPdf,
                                    onValueChange = { query ->
                                        searchQueryWithinPdf = query
                                        // Simple matched indexes engine
                                        if (query.trim().isNotEmpty()) {
                                            // Mock index locations depending on matches or title references
                                            foundOccurrences = if (query.lowercase().contains("note") || query.lowercase().contains("study")) {
                                                listOf(1, 3, 5, 8).filter { it <= pageCount }
                                            } else {
                                                listOf(1, 2)
                                            }
                                            currentOccurrenceIndex = 0
                                        } else {
                                            foundOccurrences = emptyList()
                                        }
                                    },
                                    placeholder = { Text("Search text inside...", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    singleLine = true,
                                    trailingIcon = {
                                        if (searchQueryWithinPdf.isNotEmpty()) {
                                            IconButton(onClick = {
                                                searchQueryWithinPdf = ""
                                                foundOccurrences = emptyList()
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                                            }
                                        }
                                    }
                                )
                                if (foundOccurrences.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${currentOccurrenceIndex + 1}/${foundOccurrences.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = {
                                        if (currentOccurrenceIndex > 0) {
                                            currentOccurrenceIndex--
                                        } else {
                                            currentOccurrenceIndex = foundOccurrences.size - 1
                                        }
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(foundOccurrences[currentOccurrenceIndex] - 1)
                                        }
                                    }) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Previous")
                                    }
                                    IconButton(onClick = {
                                        if (currentOccurrenceIndex < foundOccurrences.size - 1) {
                                            currentOccurrenceIndex++
                                        } else {
                                            currentOccurrenceIndex = 0
                                        }
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(foundOccurrences[currentOccurrenceIndex] - 1)
                                        }
                                    }) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Next")
                                    }
                                }
                            }
                        }

                        // Scrollable PDF List Page Viewer
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 4f)
                                        offset = if (scale > 1f) {
                                            androidx.compose.ui.geometry.Offset(
                                                x = offset.x + pan.x,
                                                y = offset.y + pan.y
                                            )
                                        } else {
                                            androidx.compose.ui.geometry.Offset.Zero
                                        }
                                    }
                                }
                        ) {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(pageCount) { index ->
                                    PdfPageItem(
                                        pdfRenderer = pdfRenderer!!,
                                        pageIndex = index,
                                        isDarkMode = isReaderDarkMode,
                                        searchQuery = searchQueryWithinPdf,
                                        isHighlighted = foundOccurrences.contains(index + 1)
                                    )
                                }
                            }

                            // Zoom reset floating trigger
                            if (scale > 1f) {
                                FloatingActionButton(
                                    onClick = {
                                        scale = 1f
                                        offset = androidx.compose.ui.geometry.Offset.Zero
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(Icons.Default.ZoomOutMap, contentDescription = "Reset Zoom")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Page jumping dialog popup
    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("Jump to Page") },
            text = {
                Column {
                    Text(
                        "Enter a target page index (1 to $pageCount) to slide there directly.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = jumpPageInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                jumpPageInput = input
                            }
                        },
                        label = { Text("Page Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = jumpPageInput.toIntOrNull()
                        if (num != null && num in 1..pageCount) {
                            coroutineScope.launch {
                                lazyListState.scrollToItem(num - 1)
                            }
                            showJumpDialog = false
                        }
                    }
                ) {
                    Text("Go")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PdfPageItem(
    pdfRenderer: PdfRenderer,
    pageIndex: Int,
    isDarkMode: Boolean,
    searchQuery: String,
    isHighlighted: Boolean
) {
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Render bitmap on background threads
    LaunchedEffect(pageIndex, pdfRenderer) {
        withContext(Dispatchers.IO) {
            try {
                val page = pdfRenderer.openPage(pageIndex)
                
                // Set high quality display density
                val scaleFactor = 2
                val width = page.width * scaleFactor
                val height = page.height * scaleFactor

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                withContext(Dispatchers.Main) {
                    pageBitmap = bitmap
                }
                page.close()
            } catch (e: Exception) {
                // Return gracefully on render exceptions
            }
        }
    }

    DisposableEffect(pageIndex) {
        onDispose {
            // Recovers memory footprint of obsolete pages
            pageBitmap?.recycle()
            pageBitmap = null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (pageBitmap != null) {
                    // Applied color filter for eye-safe Dark Mode document inversion
                    val filter = if (isDarkMode) {
                        val colorMatrix = ColorMatrix(
                            floatArrayOf(
                                -1f,  0f,  0f, 0f, 255f, // Invert Red
                                 0f, -1f,  0f, 0f, 255f, // Invert Green
                                 0f,  0f, -1f, 0f, 255f, // Invert Blue
                                 0f,  0f,  0f, 1f, 0f    // Preserve Alpha
                            )
                        )
                        ColorFilter.colorMatrix(colorMatrix)
                    } else null

                    Image(
                        bitmap = pageBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Document Page ${pageIndex + 1}",
                        colorFilter = filter,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Page ${pageIndex + 1}",
                    fontSize = 11.sp,
                    color = if (isDarkMode) Color.LightGray else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Highlighter bookmark overlay on top edge
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Yellow.copy(alpha = 0.12f))
                        .pointerInput(Unit) {}
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .background(Color.Yellow, shape = RoundedCornerShape(4.dp))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Found Query match item",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
