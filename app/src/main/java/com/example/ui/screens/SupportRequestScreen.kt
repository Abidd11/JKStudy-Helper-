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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Student Help Desk", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            // Elegant modern pills tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SupportTab.values().forEach { tab ->
                    val isSelected = activeTabState == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { activeTabState = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (tab == SupportTab.RequestForm) Icons.Default.PostAdd else Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                            // Intro Help Card with elegant soft layout tint
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.SupportAgent,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Can't find your subject notes?",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "Submit resource demands below. Our state curriculum coordinators will source, index, and load papers directly over-the-air!",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Form Inputs Card with clean styling
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(20.dp)
                                        ),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            "NEW MATERIAL REQUEST FORM",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.8.sp
                                        )
                                        
                                        OutlinedTextField(
                                            value = inputName,
                                            onValueChange = { inputName = it },
                                            label = { Text("Your Name", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth().testTag("request_name_input"),
                                            placeholder = { Text("Enter your full name") },
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            leadingIcon = { 
                                                Icon(
                                                    Icons.Default.Person, 
                                                    contentDescription = null, 
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                ) 
                                            }
                                        )

                                        OutlinedTextField(
                                            value = inputEmail,
                                            onValueChange = { inputEmail = it },
                                            label = { Text("Email Address (Optional)", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth().testTag("request_email_input"),
                                            placeholder = { Text("Enter email for alert notifications") },
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            leadingIcon = { 
                                                Icon(
                                                    Icons.Default.Email, 
                                                    contentDescription = null, 
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                ) 
                                            }
                                        )

                                        OutlinedTextField(
                                            value = inputDetails,
                                            onValueChange = { inputDetails = it },
                                            label = { Text("Material Details & Requirements", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(115.dp)
                                                .testTag("request_details_input"),
                                            placeholder = { Text("e.g. Class 12 JKBOSE Physics Chapter 4 handwritten notes and 2024 solved questions...") },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            leadingIcon = { 
                                                Icon(
                                                    Icons.Default.MenuBook, 
                                                    contentDescription = null, 
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                ) 
                                            }
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
                                                .height(48.dp)
                                                .testTag("submit_request_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Submit Demand Request", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            // Dynamic Live History list previously lost & not rendering
                            if (materialRequests.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "YOUR SUBMISSION HISTORY (${materialRequests.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.8.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                items(materialRequests.reversed()) { request ->
                                    RequestItemRowRedesigned(request)
                                }
                            }
                        }
                    }

                    SupportTab.Faqs -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.HelpOutline, 
                                                contentDescription = null, 
                                                tint = MaterialTheme.colorScheme.secondary, 
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Student-First Helper Guide", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Answers relating to downloads, offline library, and syllabus queries.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                }
                            }

                            val faqList = listOf(
                                FaqItem(
                                    "How do I read downloaded guess papers and materials offline?",
                                    "When you click 'One Click Download' on any material card, the study PDF gets saved directly to your secure application offline sandbox paths. You can access all of your saved files instantly at any time from the 'Library'/Saved tab on the bottom bar navigation."
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
fun RequestItemRowRedesigned(request: MaterialRequestEntity) {
    val dateStr = remember(request.timestamp) {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(Date(request.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 13.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateStr, 
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!request.email.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = request.email, 
                    fontSize = 11.sp, 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = request.details, 
                fontSize = 12.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sourcing Pending (Verifying global server catalog)",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
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
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
