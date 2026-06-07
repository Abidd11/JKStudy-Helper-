package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MaterialRequestEntity
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportRequestScreen(
    viewModel: StudyViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val materialRequests by viewModel.materialRequests.collectAsState()

    var activeTabState by remember { mutableStateOf(SupportTab.RequestForm) }

    // Form inputs state
    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputDetails by remember { mutableStateOf("") }

    // Search query for requests if we have any
    var searchRequestQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support & Request Desk", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
            // Tab Header Selection (Request Study Material, Support FAQs)
            TabRow(
                selectedTabIndex = activeTabState.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SupportTab.values().forEach { tab ->
                    Tab(
                        selected = activeTabState == tab,
                        onClick = { activeTabState = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (activeTabState == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            AnimatedContent(
                targetState = activeTabState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "support_content_animation"
            ) { targetTab ->
                when (targetTab) {
                    SupportTab.RequestForm -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Intro Help Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp)),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.HelpCenter,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Cant find your subject notes?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                "Fill in the request details below. Our study coordination team will source the paper, notes, or books and add it directly to JK Study Helper!",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Form Inputs
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp)),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("Request Form", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        
                                        OutlinedTextField(
                                            value = inputName,
                                            onValueChange = { inputName = it },
                                            label = { Text("Your Name", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("e.g. Rahul Sharma") },
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        )

                                        OutlinedTextField(
                                            value = inputEmail,
                                            onValueChange = { inputEmail = it },
                                            label = { Text("Email Address (Optional)", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("e.g. rahul@gmail.com") },
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        )

                                        OutlinedTextField(
                                            value = inputDetails,
                                            onValueChange = { inputDetails = it },
                                            label = { Text("Resource Details", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp),
                                            placeholder = { Text("e.g. Class 12 JKBOSE Physics Chapter 4 handwritten notes and 2024 solved questions...") },
                                            shape = RoundedCornerShape(12.dp),
                                            leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        )

                                        Button(
                                            onClick = {
                                                if (inputName.trim().isEmpty()) {
                                                    Toast.makeText(context, "Please fill in your Name.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val detailsText = if (inputDetails.trim().isEmpty()) "Requested Study Material and reference guides." else inputDetails
                                                    viewModel.submitMaterialRequest(inputName, inputEmail, detailsText) {
                                                        Toast.makeText(context, "Request submitted & logged successfully!", Toast.LENGTH_SHORT).show()
                                                        inputName = ""
                                                        inputEmail = ""
                                                        inputDetails = ""
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Submit Help Desk Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            // Request History Title
                            item {
                                Text(
                                    text = "Your Request History (${materialRequests.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            if (materialRequests.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("No requests submitted yet.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            } else {
                                items(materialRequests.reversed()) { request ->
                                    RequestItemRow(request)
                                }
                            }
                        }
                    }

                    SupportTab.Faqs -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Student-First Helper Guide", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Frequently Asked Questions relating to board syllabus, downloads, and app usage.", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            val faqList = listOf(
                                FaqItem(
                                    "How do I read downloaded guess papers and materials offline?",
                                    "When you click 'One Click Download' on any material card, the study PDF gets saved directly to your secure application offline sandbox paths. You can access all of your saved files instantly at any time from the 'Library' tab on the home screen."
                                ),
                                FaqItem(
                                    "Is JK Study Helper totally free of cost?",
                                    "Yes! All books, previous year question papers (PYQs), guess papers, and hand-written syllabus summaries are 100% free of cost. We believe in providing democratic access to high-quality exam preparations for state boards and national competitive exams."
                                ),
                                FaqItem(
                                    "How does the built-in PDF Reader work?",
                                    "You do not need to install any external PDF application! Clicking 'Open in Built-In Reader' opens a responsive local document reader where you can search text instantly, add bookmarks, track page read, and scale/zoom PDFs smoothly."
                                ),
                                FaqItem(
                                    "Where does the study material come from?",
                                    "All content on JK Study Helper is fetched dynamically using our synchronized school database. This updates the material roster instantly without requiring any manual store app updates!"
                                ),
                                FaqItem(
                                    "How can I suggest custom content?",
                                    "Go to the 'Material Request' tab on this Support Screen, write details of your target papers or books, and tap submit. Our team will index it and sync it over the air dynamically."
                                )
                            )

                            items(faqList) { faq ->
                                FaqCard(faq)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItemRow(request: MaterialRequestEntity) {
    val dateStr = remember(request.timestamp) {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(Date(request.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
            }
            if (!request.email.isNullOrEmpty()) {
                Text(request.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(request.details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFFF9800), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Sourcing Pending (Verifying global servers catalog)",
                    fontSize = 9.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FaqCard(faq: FaqItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

data class FaqItem(
    val question: String,
    val answer: String
)

enum class SupportTab(val title: String) {
    RequestForm("Material Request"),
    Faqs("Help & FAQs")
}
