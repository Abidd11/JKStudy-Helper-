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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyMaterial
import com.example.data.repository.StudyState
import com.example.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    onCategoryClick: (String) -> Unit,
    onMaterialClick: (StudyMaterial) -> Unit,
    onMyDownloadsClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    val materialsState by viewModel.materialsState.collectAsState()
    val latestMaterials by viewModel.latestMaterials.collectAsState()
    val trendingMaterials by viewModel.trendingMaterials.collectAsState()
    val isServerOnline by viewModel.isServerOnline.collectAsState()

    val materials = remember(materialsState) {
        if (materialsState is StudyState.Success) {
            (materialsState as StudyState.Success).materials
        } else {
            viewModel.getMaterialsFromCache()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Welcoming card & top header with server health light
        item {
            HeaderWelcomeSection(isServerOnline, materials.size, onMyDownloadsClick)
        }

        // Horizontal Tray: Latest Materials Section on Top of other controls!
        item {
            LatestMaterialsTray(latestMaterials, onMaterialClick)
        }

        // Motivational Quote Card
        item {
            QuoteCard(viewModel.dailyMotivationalQuote)
        }

        // Beautiful, organized Categories Hub Section
        item {
            StudyCategoriesHub(
                materials = materials,
                onCategoryClick = onCategoryClick,
                onSupportClick = onSupportClick
            )
        }

        // Column: Trending / Most Visited Downloads Section
        item {
            TrendingSection(trendingMaterials, onMaterialClick)
        }
    }
}

@Composable
fun HeaderWelcomeSection(
    isServerOnline: Boolean,
    totalCount: Int,
    onMyDownloadsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
            .padding(16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "JK Study Helper",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Total materials label in full Material 3 style badge indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Total Library",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$totalCount Files",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SlidingBannerView(
    activeBanner: BannerItem,
    totalBannersNum: Int,
    currentIndex: Int,
    onIndicatorClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = activeBanner.bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = activeBanner.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeBanner.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            // Slide indicators on bottom center
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until totalBannersNum) {
                    Box(
                        modifier = Modifier
                            .size(if (i == currentIndex) 16.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(if (i == currentIndex) Color.White else Color.White.copy(alpha = 0.4f))
                            .clickable { onIndicatorClick(i) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudyCategoriesHub(
    materials: List<StudyMaterial>,
    onCategoryClick: (String) -> Unit,
    onSupportClick: () -> Unit
) {
    val schoolClasses = remember {
        listOf(
            CategoryCard("Class 8", "class8", Icons.Default.School, Color(0xFFF57C00), Color(0xFFFFA726), "Middle School Notes"),
            CategoryCard("Class 9", "class9", Icons.Default.School, Color(0xFF2E7D32), Color(0xFF66BB6A), "High School Prep"),
            CategoryCard("Class 10", "class10", Icons.Default.School, Color(0xFF00838F), Color(0xFF26C6DA), "Secondary Boards"),
            CategoryCard("Class 11", "class11", Icons.Default.AutoStories, Color(0xFF6A1B9A), Color(0xFFAB47BC), "Senior Secondary"),
            CategoryCard("Class 12", "class12", Icons.Default.AutoStories, Color(0xFF1565C0), Color(0xFF42A5F5), "Higher Secondary")
        )
    }

    val examPrep = remember {
        listOf(
            CategoryCard("JKBOSE", "jkbose", Icons.Default.MenuBook, Color(0xFF283593), Color(0xFF5C6BC0), "J&K Board Syllabus"),
            CategoryCard("CBSE", "cbse", Icons.Default.Quiz, Color(0xFF00695C), Color(0xFF26A69A), "National Curriculum"),
            CategoryCard("NEET", "neet", Icons.Default.FitnessCenter, Color(0xFFC2185B), Color(0xFFEC407A), "Medical Exam Guide"),
            CategoryCard("JEE", "jee", Icons.Default.Architecture, Color(0xFF37474F), Color(0xFF78909C), "Engineering Guide"),
            CategoryCard("CUET", "cuet", Icons.Default.MenuBook, Color(0xFFD84315), Color(0xFFFF7043), "Central University"),
            CategoryCard("SSC", "ssc", Icons.Default.Assignment, Color(0xFF4E342E), Color(0xFF8D6E63), "Staff Selection Commission"),
            CategoryCard("UPSC", "upsc", Icons.Default.Quiz, Color(0xFFD32F2F), Color(0xFFEF5350), "IAS/IFS Preparations")
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Section Title
        Text(
            text = "Study Categories Hub",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Group 1: Academic Classes
        Text(
            text = "ACADEMIC SCHOOL CLASSES",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        CategoryCardGrid(schoolClasses, materials, onCategoryClick)

        Spacer(modifier = Modifier.height(14.dp))

        // Group 2: Board & Competitive Prep
        Text(
            text = "BOARDS & COMPETITIVE PREP",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
        CategoryCardGrid(examPrep, materials, onCategoryClick)
    }
}

@Composable
fun CategoryCardGrid(
    items: List<CategoryCard>,
    materials: List<StudyMaterial>,
    onCategoryClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val pairs = items.chunked(2)
        pairs.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { card ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryHubTile(card, materials, onCategoryClick)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryHubTile(
    card: CategoryCard,
    materials: List<StudyMaterial>,
    onCategoryClick: (String) -> Unit
) {
    val count = remember(materials, card.code) {
        materials.count { it.matchesCategory(card.code) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp))
            .clickable { onCategoryClick(card.code) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(card.startColor, card.endColor)
                    )
                )
        ) {
            // High transparency background vector symbol (Category cover image)
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
            )

            // Dynamic text descriptors overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = card.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.description,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (count == 1) "1 Study File" else "$count Study Files",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

data class CategoryCard(
    val title: String,
    val code: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color,
    val description: String
)

@Composable
fun QuoteCard(quote: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(
                    "MOTIVATION FOR TODAY",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = quote,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LatestMaterialsTray(
    items: List<StudyMaterial>,
    onMaterialClick: (StudyMaterial) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Latest Materials",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Recent Added",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { material ->
                    Card(
                        modifier = Modifier
                            .width(165.dp)
                            .heightIn(min = 145.dp)
                            .clickable { onMaterialClick(material) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = material.inferMaterialTypeString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = material.fileName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 14.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${material.fileSize} MB",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = material.dateString,
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingSection(
    items: List<StudyMaterial>,
    onMaterialClick: (StudyMaterial) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Trending Downloads",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sorting trending elements...", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.take(5).forEachIndexed { index, material ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onMaterialClick(material) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Position index marker circle
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = material.fileName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = material.inferCategoryString(),
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

                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyMaterialRowItem(
    material: StudyMaterial,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = material.fileName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                Text(
                    text = material.inferMaterialTypeString(),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${material.fileSize} MB",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

// Data Classes
data class BannerItem(
    val title: String,
    val description: String,
    val bgColor: Color
)

data class QuickGridItem(
    val label: String,
    val icon: ImageVector,
    val routeCode: String
)
