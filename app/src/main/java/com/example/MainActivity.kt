package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.StudyDatabase
import com.example.data.remote.StudyApiService
import com.example.data.repository.StudyRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning

class MainActivity : ComponentActivity() {
    companion object {
        private var activityRef: java.lang.ref.WeakReference<MainActivity>? = null
        fun getActivity(): MainActivity? = activityRef?.get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRef = java.lang.ref.WeakReference(this)
        enableEdgeToEdge()

        // Initialize Unity Ads SDK with real ad serves
        com.example.ui.ads.AdManager.initialize(this)

        // 1. Instantiate Core Architecture Components (Local DB, Retrofit Engine, Repository)
        val database = StudyDatabase.getDatabase(this)
        val apiService = StudyApiService.create()
        val repository = StudyRepository(
            context = this,
            apiService = apiService,
            downloadDao = database.downloadDao(),
            favoriteDao = database.favoriteDao(),
            recentDao = database.recentDao(),
            requestDao = database.requestDao()
        )

        // 2. Setup ViewModel Factories using standard Android injection
        val vmFactory = StudyViewModelFactory(application, repository)

        setContent {
            MyApplicationTheme {
                val viewModel: StudyViewModel = viewModel(factory = vmFactory)
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val topLevelRoutes = listOf(ROUTE_HOME, ROUTE_SEARCH, ROUTE_ALL_MATERIALS, ROUTE_MY_DOWNLOADS, ROUTE_SETTINGS)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in topLevelRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth(),
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == ROUTE_HOME,
                                    alwaysShowLabel = true,
                                    onClick = {
                                        if (currentRoute != ROUTE_HOME) {
                                            navController.navigate(ROUTE_HOME) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == ROUTE_SEARCH,
                                    alwaysShowLabel = true,
                                    onClick = {
                                        if (currentRoute != ROUTE_SEARCH) {
                                            navController.navigate(ROUTE_SEARCH) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search"
                                        )
                                    },
                                    label = { Text("Search", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == ROUTE_ALL_MATERIALS,
                                    alwaysShowLabel = true,
                                    onClick = {
                                        if (currentRoute != ROUTE_ALL_MATERIALS) {
                                            navController.navigate(ROUTE_ALL_MATERIALS) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = "Files"
                                        )
                                    },
                                    label = { Text("Files", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == ROUTE_MY_DOWNLOADS,
                                    alwaysShowLabel = true,
                                    onClick = {
                                        if (currentRoute != ROUTE_MY_DOWNLOADS) {
                                            navController.navigate(ROUTE_MY_DOWNLOADS) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.FolderSpecial,
                                            contentDescription = "Saved"
                                        )
                                    },
                                    label = { Text("Saved", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == ROUTE_SETTINGS,
                                    alwaysShowLabel = true,
                                    onClick = {
                                        if (currentRoute != ROUTE_SETTINGS) {
                                            navController.navigate(ROUTE_SETTINGS) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val isShowingAdLoader by viewModel.isShowingAdLoader.collectAsState()
                    val adError by viewModel.adError.collectAsState()

                    if (isShowingAdLoader) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Ad Loading...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            text = {
                                Text(
                                    "Please wait while we prepare a video ad. Watching ads helps us keep study materials free for everyone!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            confirmButton = {},
                            dismissButton = {},
                            properties = androidx.compose.ui.window.DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false
                            )
                        )
                    }

                    if (adError != null) {
                        AlertDialog(
                            onDismissRequest = { viewModel.clearAdError() },
                            title = {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Ad Failure",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        "Ad Load Failed",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            text = {
                                Text(
                                    "To keep our high-quality study materials completely free, watching a video ad is required before downloading.\n\nError: ${adError}\n\nPlease check your internet connection and try again.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { viewModel.clearAdError() }) {
                                    Text("Dismiss", fontWeight = FontWeight.Bold)
                                }
                            },
                            properties = androidx.compose.ui.window.DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_SPLASH,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        // Startup Splash Screen
                        composable(ROUTE_SPLASH) {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate(ROUTE_HOME) {
                                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Home Screen Destination
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                onCategoryClick = { categoryCode ->
                                    navController.navigate("category/$categoryCode")
                                },
                                onMaterialClick = { material ->
                                    val safeCategory = material.inferCategoryString()
                                        .lowercase()
                                        .replace(" ", "")
                                        .trim()
                                    navController.navigate("category/$safeCategory")
                                },
                                onMyDownloadsClick = {
                                    navController.navigate(ROUTE_MY_DOWNLOADS) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onSupportClick = {
                                    navController.navigate(ROUTE_SUPPORT)
                                }
                            )
                        }

                        // Dedicated Search Destination
                        composable(ROUTE_SEARCH) {
                            SearchScreen(
                                viewModel = viewModel,
                                onMaterialClick = { material ->
                                    viewModel.trackRecentView(material.fileId)
                                    viewModel.triggerDownload(material) {
                                        navController.navigate("pdf_viewer/${material.fileId}")
                                    }
                                }
                            )
                        }

                        // Dedicated All Materials Destination
                        composable(ROUTE_ALL_MATERIALS) {
                            AllMaterialsScreen(
                                viewModel = viewModel,
                                onMaterialClick = { material ->
                                    viewModel.trackRecentView(material.fileId)
                                    viewModel.triggerDownload(material) {
                                        navController.navigate("pdf_viewer/${material.fileId}")
                                    }
                                }
                            )
                        }

                        // Category Materials Listings
                        composable("category/{categoryName}") { backStackEntry ->
                            val categoryCode = backStackEntry.arguments?.getString("categoryName") ?: "class10"
                            CategoryMaterialsScreen(
                                categoryCode = categoryCode,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onOpenPdfReader = { fileId ->
                                    navController.navigate("pdf_viewer/$fileId")
                                }
                            )
                        }

                        // Native interactive PDF Reader
                        composable("pdf_viewer/{fileId}") { backStackEntry ->
                            val fileId = backStackEntry.arguments?.getString("fileId") ?: ""
                            PdfReaderScreen(
                                fileId = fileId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        // Download Cabinet Drawer
                        composable(ROUTE_MY_DOWNLOADS) {
                            MyDownloadsScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onOpenPdfReader = { fileId ->
                                    navController.navigate("pdf_viewer/$fileId")
                                }
                            )
                        }

                        // Support request screen
                        composable(ROUTE_SUPPORT) {
                            SupportRequestScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        // Settings and Help Desk Screen
                        composable(ROUTE_SETTINGS) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onOpenSupportClick = {
                                    navController.navigate(ROUTE_SUPPORT)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (activityRef?.get() == this) {
            activityRef = null
        }
        super.onDestroy()
    }
}

// Standalone Custom Provider Factory
class StudyViewModelFactory(
    private val application: Application,
    private val repository: StudyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel registration")
    }
}

// Route Configuration keys
const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home"
const val ROUTE_SEARCH = "search"
const val ROUTE_ALL_MATERIALS = "all_materials"
const val ROUTE_MY_DOWNLOADS = "my_downloads"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_SUPPORT = "support"

