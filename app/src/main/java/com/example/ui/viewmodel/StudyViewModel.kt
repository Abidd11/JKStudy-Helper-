package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DownloadEntity
import com.example.data.local.MaterialRequestEntity
import com.example.data.model.StudyMaterial
import com.example.data.repository.StudyRepository
import com.example.data.repository.StudyState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyViewModel(
    application: Application,
    private val repository: StudyRepository
) : AndroidViewModel(application) {

    // Material State (Flat sheets list)
    val materialsState: StateFlow<StudyState> = repository.materialsState

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Room Streams
    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val favorites: StateFlow<List<String>> = repository.allFavorites
        .map { list -> list.map { it.fileId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recents: StateFlow<List<String>> = repository.allRecents
        .map { list -> list.map { it.fileId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materialRequests: StateFlow<List<MaterialRequestEntity>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Correlated Flow wrappers for lists
    val favoriteMaterials: StateFlow<List<StudyMaterial>> = combine(materialsState, favorites) { state, favIds ->
        if (state is StudyState.Success) {
            state.materials.filter { favIds.contains(it.fileId) }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMaterials: StateFlow<List<StudyMaterial>> = combine(materialsState, recents) { state, recIds ->
        if (state is StudyState.Success) {
            // Map according to recIds index order to preserve reverse chronology
            recIds.mapNotNull { id -> state.materials.find { it.fileId == id } }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMaterials: StateFlow<List<StudyMaterial>> = materialsState.map { state ->
        if (state is StudyState.Success) {
            state.materials.sortedByDescending { it.timestamp }.take(15)
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingMaterials: StateFlow<List<StudyMaterial>> = materialsState.map { state ->
        if (state is StudyState.Success) {
            // Simulated trending files by sorting by size & age variance
            state.materials.sortedWith(compareBy({ -it.fileSize }, { -it.timestamp })).take(10)
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active downloading states
    private val _downloadingFileId = MutableStateFlow<String?>(null)
    val downloadingFileId: StateFlow<String?> = _downloadingFileId.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadNotification = MutableStateFlow<String?>(null)
    val downloadNotification: StateFlow<String?> = _downloadNotification.asStateFlow()

    private val _isShowingAdLoader = MutableStateFlow(false)
    val isShowingAdLoader: StateFlow<Boolean> = _isShowingAdLoader.asStateFlow()

    private val _adError = MutableStateFlow<String?>(null)
    val adError: StateFlow<String?> = _adError.asStateFlow()

    private val _showSponsorAd = MutableStateFlow<StudyMaterial?>(null)
    val showSponsorAd: StateFlow<StudyMaterial?> = _showSponsorAd.asStateFlow()

    private var pendingDownloadCompleteCallback: ((com.example.data.local.DownloadEntity) -> Unit)? = null

    fun clearAdError() {
        _adError.value = null
    }

    fun dismissSponsorAd() {
        _showSponsorAd.value = null
        pendingDownloadCompleteCallback = null
    }

    fun completeSponsorAd() {
        val material = _showSponsorAd.value
        val callback = pendingDownloadCompleteCallback
        _showSponsorAd.value = null
        pendingDownloadCompleteCallback = null
        if (material != null && callback != null) {
            viewModelScope.launch {
                executeDownloadTask(material, callback)
            }
        }
    }

    // Server health indicator
    val isServerOnline: StateFlow<Boolean> = materialsState.map {
        it is StudyState.Success
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        // Initial refresh
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshMaterials()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getMaterialsFromCache(): List<StudyMaterial> = repository.getMaterialsFromCache()

    suspend fun getDownloadById(fileId: String): DownloadEntity? = repository.getDownloadById(fileId)

    // Toggle Favorites Action
    fun toggleFavorite(fileId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(fileId)
        }
    }

    fun isFavorite(fileId: String): Flow<Boolean> = repository.isFavoriteFlow(fileId)

    // Mark Recent action
    fun trackRecentView(fileId: String) {
        viewModelScope.launch {
            repository.addMaterialToRecent(fileId)
        }
    }

    // Material request submit
    fun submitMaterialRequest(name: String, email: String, details: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.submitRequest(name, email, details)
            onSuccess()
        }
    }

    // Initiate down-stream file downloads with video ad integrations
    fun triggerDownload(material: StudyMaterial, onComplete: (com.example.data.local.DownloadEntity) -> Unit) {
        val currentActivity = com.example.MainActivity.getActivity()
        if (currentActivity != null) {
            _isShowingAdLoader.value = true
            _adError.value = null
            com.example.ui.ads.AdManager.showAd(
                currentActivity,
                onAdComplete = {
                    _isShowingAdLoader.value = false
                    viewModelScope.launch {
                        executeDownloadTask(material, onComplete)
                    }
                },
                onAdFailed = { errorMsg ->
                    _isShowingAdLoader.value = false
                    // Fall back cleanly to high-fidelity Sponsor Ad when local real Start.io ad fails to load!
                    android.util.Log.w("StudyViewModel", "Real Ad Failed: $errorMsg. Falling back to Sponsor Ad.")
                    pendingDownloadCompleteCallback = onComplete
                    _showSponsorAd.value = material
                }
            )
        } else {
            pendingDownloadCompleteCallback = onComplete
            _showSponsorAd.value = material
        }
    }

    // Interactive Reward Ad player triggered on clicking downloaded files to satisfy 'reward ads every click'
    fun triggerAdForOpening(material: StudyMaterial, onComplete: () -> Unit) {
        val currentActivity = com.example.MainActivity.getActivity()
        if (currentActivity != null) {
            _isShowingAdLoader.value = true
            _adError.value = null
            com.example.ui.ads.AdManager.showAd(
                currentActivity,
                onAdComplete = {
                    _isShowingAdLoader.value = false
                    onComplete()
                },
                onAdFailed = { errorMsg ->
                    _isShowingAdLoader.value = false
                    android.util.Log.w("StudyViewModel", "Real Opening Ad Failed: $errorMsg. Falling back to normal open.")
                    // Graceful fallback to Interstitial is handled in AdManager or direct callback here
                    onComplete()
                }
            )
        } else {
            onComplete()
        }
    }

    private suspend fun executeDownloadTask(
        material: StudyMaterial,
        onComplete: (com.example.data.local.DownloadEntity) -> Unit
    ) {
        _downloadingFileId.value = material.fileId
        _downloadProgress.value = 0f
        try {
            val downloadEntity = repository.downloadStudyMaterial(material) { progress ->
                _downloadProgress.value = progress
            }
            _downloadNotification.value = "Download complete: ${material.fileName}"
            _downloadingFileId.value = null
            onComplete(downloadEntity)
        } catch (e: Exception) {
            _downloadingFileId.value = null
            _downloadNotification.value = "Download failed: ${e.localizedMessage}"
        }
    }

    fun clearNotification() {
        _downloadNotification.value = null
    }

    fun deleteDownloadedFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteDownload(fileId)
        }
    }

    // PDF specific bookmark managers
    fun saveLastOpenedPage(fileId: String, page: Int) {
        viewModelScope.launch {
            repository.updateLastOpenedPage(fileId, page)
        }
    }

    fun togglePageBookmark(fileId: String, pageNum: Int) {
        viewModelScope.launch {
            val download = repository.getDownloadById(fileId)
            if (download != null) {
                val currentList = if (download.bookmarkPages.isEmpty()) {
                    emptyList()
                } else {
                    download.bookmarkPages.split(",").mapNotNull { it.toIntOrNull() }.toMutableList()
                }

                val newList = currentList.toMutableList()
                if (newList.contains(pageNum)) {
                    newList.remove(pageNum)
                } else {
                    newList.add(pageNum)
                }

                repository.updateBookmarks(fileId, newList)
            }
        }
    }

    // Static quotes & affairs
    val dailyMotivationalQuote: String by lazy {
        quotes.random()
    }

    val dailyCurrentAffairs: List<CurrentAffairItem> = listOf(
        CurrentAffairItem(
            title = "India, France sign landmark Defense Industrial Roadmap Agreement",
            category = "International Affairs",
            date = "07/06/2026",
            details = "Both nations have committed to co-develop key aviation components, drone structures, and defense electronic modules to boost bilateral military coordination and localized manufacturing."
        ),
        CurrentAffairItem(
            title = "UGC announces dual enrollment options for research degree paths",
            category = "National Education",
            date = "06/06/2026",
            details = "The University Grants Commission has established formal guidelines permitting students to enroll in parallel research certifications and professional courses offline and online."
        ),
        CurrentAffairItem(
            title = "Jammu and Kashmir establishes standard tech hubs for schools",
            category = "Local Governance",
            date = "05/06/2026",
            details = "JK Governance launches a high-speed broadband network plan connecting all senior secondary government institutions with smart laboratory labs and learning tablets."
        ),
        CurrentAffairItem(
            title = "ISRO successfully deploys indigenous weather-monitoring sat INSAT-4G",
            category = "Science & Tech",
            date = "04/06/2026",
            details = "The Indian Space Research Organisation launched the GSLV-F14 carrying advanced meteorological equipment designed to monitor shifting cloud fronts, cyclone formations, and localized precipitation models."
        ),
        CurrentAffairItem(
            title = "Reserve Bank of India extends pilot of Central Bank Digital Currency (e-Rupee)",
            category = "Economic News",
            date = "03/06/2026",
            details = "The RBI expands merchant programs and wholesale credit clearing paths utilizing digital rupee protocols across all major regional banking cooperatives."
        )
    )

    private val quotes = listOf(
        "\"The secret of getting ahead is getting started.\"- Mark Twain",
        "\"Believe you can and you're halfway there.\"- Theodore Roosevelt",
        "\"Your limit is only your imagination. Keep moving forward!\"",
        "\"Every accomplishment starts with the decision to try.\"",
        "\"Success is not final, failure is not fatal: it is the courage to continue that counts.\""
    )

    // Maintenance / App update check engine state
    private val _updateState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateState: StateFlow<UpdateCheckState> = _updateState.asStateFlow()

    // Dynamic GitHub-hosted User Manual engine
    private val _rawManual = MutableStateFlow<String>("")
    val rawManual: StateFlow<String> = _rawManual.asStateFlow()

    private val _isManualLoading = MutableStateFlow<Boolean>(false)
    val isManualLoading: StateFlow<Boolean> = _isManualLoading.asStateFlow()

    fun loadAppManual() {
        _isManualLoading.value = true
        viewModelScope.launch {
            val url = "https://raw.githubusercontent.com/Abidd11/Jkbose/refs/heads/main/manual.md"
            val fallbackDocs = """
                📌 **JK STUDY HELPER - LIVE MANUAL**
                
                Welcome to J&K Study Helper, your premium community platform for boards and competitive examinations offline & online.
                
                ---
                
                🚀 **ONE CLICK DIRECT DOWNLOADS**
                - To view any resource, simply click the card or the **"One Click Download"** action button.
                - We automatically detect if you have already saved the file offline.
                - If already downloaded, the interactive PDF reader will open **INSTANTLY without any ads or video streams!**
                
                ---
                
                🎟️ **REWARDED AD BLOCKERS & MAINTENANCE**
                - To keep high-quality books, syllabi, datesheets, and papers completely free, a short sponsor ad supports our storage costs.
                - If real video servers have congestion, a fallback sponsor banner allows you to bypass and acquire downloads instantly!
                
                ---
                
                📬 **SUBMISSION HELP DESK**
                - Want us to include specific mock tests, JKBOSE guides, or NCERT solution materials?
                - Navigate to **"Saved -> Submit Requests"** or Settings and submit your custom file demands. We inspect requests daily and push materials live!
                
                ---
                
                🔗 **GITHUB LIVE SYNC**
                - This user guide manual compiles dynamically from our cloud repository.
                - Our community keeps documentation fresh even when new store releases aren't pushed.
                
                *Developed with love by Aabid for JKBOSE & JKSSB aspirants.*
            """.trimIndent()

            try {
                val downloadedText = repository.fetchRawText(url)
                if (downloadedText.trim().isNotEmpty()) {
                    _rawManual.value = downloadedText
                } else {
                    _rawManual.value = fallbackDocs
                }
            } catch (e: Exception) {
                _rawManual.value = fallbackDocs
            } finally {
                _isManualLoading.value = false
            }
        }
    }

    fun checkForUpdates() {
        _updateState.value = UpdateCheckState.Checking
        viewModelScope.launch {
            try {
                // Fetch from user-provided official board config URL with strict timeout
                val response = kotlinx.coroutines.withTimeoutOrNull(1500) {
                    val configUrl = "https://raw.githubusercontent.com/Abidd11/Jkbose/refs/heads/main/JkStudyhelper.html"
                    repository.getAppConfig(configUrl)
                }
                if (response != null && response.isNotEmpty()) {
                    val config = response[0]
                    if (config.maintenance == "true" || config.maintenance == "true\n") {
                        _updateState.value = UpdateCheckState.Maintenance(config.maintenanceMsg)
                    } else {
                        val currentAppVersion = "1.0" // Matches versionName in build.gradle.kts
                        if (currentAppVersion == config.version) {
                            _updateState.value = UpdateCheckState.UpToDate
                        } else {
                            _updateState.value = UpdateCheckState.UpdateAvailable(
                                msg = config.updateMsg,
                                link = config.link
                            )
                        }
                    }
                } else {
                    _updateState.value = UpdateCheckState.UpToDate
                }
            } catch (e: Exception) {
                android.util.Log.e("StudyViewModel", "Check update failed or timed out: ${e.message}", e)
                // Fallback gracefully so we do not block user startup when offline/no connection
                _updateState.value = UpdateCheckState.UpToDate
            }
        }
    }

    // Trigger Interstitial Video Ad before navigation or other options
    fun triggerInterstitial(onComplete: () -> Unit) {
        val currentActivity = com.example.MainActivity.getActivity()
        if (currentActivity != null) {
            _isShowingAdLoader.value = true
            _adError.value = null
            com.example.ui.ads.AdManager.showInterstitialAd(
                currentActivity,
                onAdComplete = {
                    _isShowingAdLoader.value = false
                    onComplete()
                },
                onAdFailed = { errorMsg ->
                    _isShowingAdLoader.value = false
                    android.util.Log.d("StudyViewModel", "Interstitial unavailable: $errorMsg. Continuing...")
                    onComplete() // fallback so we never block users
                }
            )
        } else {
            onComplete()
        }
    }
}

sealed interface UpdateCheckState {
    object Idle : UpdateCheckState
    object Checking : UpdateCheckState
    data class Maintenance(val msg: String) : UpdateCheckState
    data class UpdateAvailable(val msg: String, val link: String) : UpdateCheckState
    object UpToDate : UpdateCheckState
}

data class CurrentAffairItem(
    val title: String,
    val category: String,
    val date: String,
    val details: String
)
