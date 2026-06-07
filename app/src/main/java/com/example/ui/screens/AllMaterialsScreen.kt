package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyMaterial
import com.example.data.repository.StudyState
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMaterialsScreen(
    viewModel: StudyViewModel,
    onMaterialClick: (StudyMaterial) -> Unit
) {
    val materialsState by viewModel.materialsState.collectAsState()
    
    val allMaterials = remember(materialsState) {
        if (materialsState is StudyState.Success) {
            (materialsState as StudyState.Success).materials
        } else {
            viewModel.getMaterialsFromCache()
        }
    }

    var localSearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedType by remember { mutableStateOf("all") } // all, pyq, guess, note, syllabus

    val filteredMaterials = remember(allMaterials, localSearchQuery, selectedCategory, selectedType) {
        allMaterials.filter { material ->
            val matchQuery = material.fileName.lowercase().contains(localSearchQuery.lowercase())
            val matchCategory = if (selectedCategory == "all") {
                true
            } else {
                material.matchesCategory(selectedCategory)
            }
            val matchType = if (selectedType == "all") {
                true
            } else {
                material.matchesMaterialType(selectedType)
            }
            matchQuery && matchCategory && matchType
        }
    }

    val categories = listOf(
        Pair("All Categories", "all"),
        Pair("Class 8", "class8"),
        Pair("Class 9", "class9"),
        Pair("Class 10", "class10"),
        Pair("Class 11", "class11"),
        Pair("Class 12", "class12"),
        Pair("JKBOSE", "jkbose"),
        Pair("CBSE", "cbse"),
        Pair("NEET", "neet"),
        Pair("JEE", "jee"),
        Pair("CUET", "cuet"),
        Pair("SSC", "ssc"),
        Pair("UPSC", "upsc")
    )

    val types = listOf(
        Pair("All Files", "all"),
        Pair("Notes", "notes"),
        Pair("PYQs", "pyqs"),
        Pair("Guesses", "guesspapers"),
        Pair("Important Qs", "importantquestions"),
        Pair("Books", "books")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        placeholder = { Text("Search all study materials...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (localSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { localSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // Quick Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (label, code) ->
                    val isSelected = selectedCategory == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = code },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // File Type Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(types) { (label, typeCode) ->
                    val isSelected = selectedType == typeCode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = typeCode },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            // Items Count and indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredMaterials.size} records",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (localSearchQuery.isNotEmpty() || selectedCategory != "all" || selectedType != "all") {
                    TextButton(
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            localSearchQuery = ""
                            selectedCategory = "all"
                            selectedType = "all"
                        }
                    ) {
                        Text("Reset Filters", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Scrollable List of Materials
            if (filteredMaterials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No study files match your filters.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Try clearing queries or selecting 'All'.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredMaterials) { material ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .clickable { onMaterialClick(material) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Material Icon Frame
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = material.fileName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = material.inferMaterialTypeString(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }

                                            Text(
                                                text = "•  ${material.fileSize} MB",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onMaterialClick(material) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "View Guide",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
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
}
