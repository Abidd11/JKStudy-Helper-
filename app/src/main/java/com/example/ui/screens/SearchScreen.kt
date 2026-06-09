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
fun SearchScreen(
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

    var queryText by remember { mutableStateOf("") }
    val recentSearches = listOf("Physics", "Syllabus", "JKBOSE 10th", "NEET practice")
    val filterTypes = remember { listOf("All", "Notes", "Papers", "Practicals", "Syllabus") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var showFilters by remember { mutableStateOf(false) }

    val results = remember(queryText, selectedTypeFilter, allMaterials) {
        if (queryText.trim().isEmpty()) {
            emptyList()
        } else {
            allMaterials.filter {
                val matchesQuery = it.fileName.lowercase().contains(queryText.lowercase()) ||
                        it.inferMaterialTypeString().lowercase().contains(queryText.lowercase()) ||
                        it.fileId.lowercase().contains(queryText.lowercase())
                val matchesType = when (selectedTypeFilter) {
                    "Notes" -> it.inferMaterialTypeString().lowercase().contains("notes")
                    "Papers" -> it.inferMaterialTypeString().lowercase().contains("paper")
                    "Practicals" -> it.inferMaterialTypeString().lowercase().contains("practical") || it.inferMaterialTypeString().lowercase().contains("guide")
                    "Syllabus" -> it.inferMaterialTypeString().lowercase().contains("syllabus")
                    else -> true
                }
                matchesQuery && matchesType
            }
        }
    }

    Scaffold(
        bottomBar = {
            com.example.ui.ads.UnityBannerAd(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                placementId = "Banner_Android"
            )
        },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("Search files, papers, notes or subjects...", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(27.dp)
                            ),
                        shape = RoundedCornerShape(27.dp),
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = null, 
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        trailingIcon = {
                            Row(
                                modifier = Modifier.padding(end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (queryText.isNotEmpty()) {
                                    IconButton(
                                        onClick = { queryText = "" },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { showFilters = !showFilters },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Toggle Filters",
                                        tint = if (selectedTypeFilter != "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant slide-down filter chips block
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "FILTER SEARCH BY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterTypes) { type ->
                            val isSelected = selectedTypeFilter == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTypeFilter = type },
                                label = { Text(type, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }
            }

            AnimatedVisibility(visible = queryText.trim().isEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "SUGGESTED KEYWORDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentSearches) { keyword ->
                            SuggestionChip(
                                onClick = { queryText = keyword },
                                label = { Text(keyword, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FindInPage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Find study items instantly",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Type search keyword above to scan entire files repository.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = queryText.trim().isNotEmpty()) {
                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No items matching \"$queryText\"",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Found ${results.size} matches",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                            )
                        }

                        items(results) { item ->
                            StudyMaterialRowItem(item, { onMaterialClick(item) })
                        }
                    }
                }
            }
        }
    }
}
