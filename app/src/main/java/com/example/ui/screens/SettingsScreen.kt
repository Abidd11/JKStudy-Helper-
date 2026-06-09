package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StudyViewModel,
    onOpenSupportClick: () -> Unit
) {
    val context = LocalContext.current
    val isServerOnline by viewModel.isServerOnline.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val requests by viewModel.materialRequests.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    val rawManual by viewModel.rawManual.collectAsState()
    val isManualLoading by viewModel.isManualLoading.collectAsState()

    LaunchedEffect(showManualDialog) {
        if (showManualDialog) {
            viewModel.loadAppManual()
        }
    }

    if (showManualDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showManualDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Live Student Manual",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { viewModel.loadAppManual() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload Manual",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        )
                        
                        if (isManualLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Fetching live guide from GitHub...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            androidx.compose.foundation.text.selection.SelectionContainer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            text = rawManual,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showManualDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Manual", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "URL launch failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Disclaimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Disclaimer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "This application is designed for educational purposes only.\n" +
                            "It serves as a tool to collect, organize, and provide access to publicly available study materials, results, syllabi, and educational resources from official websites and verified online sources.\n\n" +
                            "The app does not host or own any of the educational content shown.\n" +
                            "All links, PDFs, and materials are sourced from official public websites and are shared solely to help students easily access information in one place.\n\n" +
                            "All content such as PDFs, notices, datesheets, and study materials shown in this app are gathered from publicly available sources \n\n" +
                            "User-submitted materials \n" +
                            "We do not represent, affiliate with, or claim any endorsement from any educational board, institution, or government body.\n" +
                            "All trademarks and materials belong to their respective owners.\n\n" +
                            "While we make every effort to ensure that the information provided is accurate and up-to-date, we make no guarantees about the completeness, reliability, or accuracy of the data.\n" +
                            "Users are encouraged to verify details from official sources before taking any decision.\n\n" +
                            "By using this app, you agree that the developer is not responsible for any errors, omissions, or outcomes resulting from the use of this information",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("I Understand", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help & Settings",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // App Identity & Header Status Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "JK Study Helper",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Version 2.4.0 (Stable M3 Edge)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatusIndicatorCol(
                                title = "Local Files",
                                detail = "${downloads.size} Files",
                                isSuccess = downloads.isNotEmpty()
                            )

                            StatusIndicatorCol(
                                title = "My Requests",
                                detail = "${requests.size} Logged",
                                isSuccess = true
                            )
                        }
                    }
                }
            }

            // Developer About section
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "DEVELOPER ABOUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Glowing golden/rainbow border on developer's vector avatar
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                            Color(0xFFF1C40F),
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_developer_avatar),
                                contentDescription = "Aabid's profile avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Aabid",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Creator Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERIFIED CREATOR & LEAD DEVELOPER 🌟",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cool stylized quote message box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Hey there! I’m Aabid, from the heart of Kashmir. With whatever little knowledge I have, I always strive to create something new—something that can make a genuine difference in the educational lives of J&K students. My true joy lies in using my tech skills to assist the community. Thank you for your support!",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Connect with Me Instantly",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive high-fidelity contact action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { openUrl("mailto:rathereditzofficial@gmail.com") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { openUrl("https://www.instagram.com/ohh_itz__a._.bid__") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)) // Instagram primary brand color
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { openUrl("https://whatsapp.com/channel/0029VajKhZ0JENy2l6mBt817") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp brand color
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Join WhatsApp Channel", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Social Media Options section
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "OFFICIAL SOCIAL CHANNELS & MEDIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SocialRowItem(
                        title = "Join WhatsApp Channel",
                        description = "Instant notifications, papers, study material & notes",
                        icon = Icons.Default.Campaign,
                        tintColor = Color(0xFF25D366),
                        onClick = { openUrl("https://whatsapp.com/channel/0029VajKhZ0JENy2l6mBt817") }
                    )

                    SocialRowItem(
                        title = "Follow on Instagram",
                        description = "Daily current affairs, MIQs & Board updates",
                        icon = Icons.Default.CameraAlt,
                        tintColor = Color(0xFFE1306C),
                        onClick = { openUrl("https://www.instagram.com/ohh_itz__a._.bid__") }
                    )

                    SocialRowItem(
                        title = "Official Support Mail",
                        description = "Get administrative help: rathereditzofficial@gmail.com",
                        icon = Icons.Default.Email,
                        tintColor = MaterialTheme.colorScheme.primary,
                        onClick = { openUrl("mailto:rathereditzofficial@gmail.com") }
                    )
                }
            }

            // Help Desk Links
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "STUDENT HELP DESK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onOpenSupportClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QuestionAnswer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = "Submit Study Notes Request",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Have custom demands? Ask desk directly.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { showManualDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = "Read Live App Manual",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Dynamic instructions sync directly from GitHub",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }

            // Stability Manual Update Check Block
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SYSTEM RELIABILITY & DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            Toast.makeText(context, "Checking server for updates...", Toast.LENGTH_SHORT).show()
                            viewModel.checkForUpdates()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = "Inspect and Check for Updates",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Query active GitHub branch config immediately",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                }
            }

            // Disclaimer Card Info Block Selection
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "APPLICATION AGREEMENTS & DISCLAIMERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { showDisclaimerDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = "App Disclaimer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Official educational resources disclaimer.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

// Inline helper for cleanly nesting Column details inside card
@Composable
fun NoblesRequestRowDetails() {
    Text(
        text = "Submit study notes request",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Have custom demands? Ask desk directly.",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun StatusIndicatorCol(
    title: String,
    detail: String,
    isSuccess: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSuccess) MaterialTheme.colorScheme.primary else Color.Red
        )
    }
}

@Composable
fun SocialRowItem(
    title: String,
    description: String,
    icon: ImageVector,
    tintColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tintColor.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FaqItemRow(question: String, answer: String) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
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
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
