package com.example.ui.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumInfo
import com.example.data.AppDatabase
import com.example.data.AppSettingsManager
import com.example.data.AppSettingsState
import com.example.data.BatchStatus
import com.example.data.DecisionType
import com.example.data.DeletionResult
import com.example.data.FilterType
import com.example.data.ItemWithAsset
import com.example.data.MediaAssetEntity
import com.example.data.MediaRepository
import com.example.data.MediaSortOrder
import com.example.data.MediaSourceInfo
import com.example.data.RemovalBatchEntity
import com.example.data.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    EXPLORE,
    REVIEW,
    HISTORY
}

data class UiState(
    val currentTab: MainTab = MainTab.HOME,
    val isSorting: Boolean = false,
    val activeSessionId: String? = null,
    val activeSession: SessionEntity? = null,
    val allActiveSessions: List<SessionEntity> = emptyList(),
    val sessionItems: List<ItemWithAsset> = emptyList(),
    val currentIndex: Int = 0,
    val pendingThrowItems: List<ItemWithAsset> = emptyList(),
    val albums: List<AlbumInfo> = emptyList(),
    val selectedAlbum: String? = null,
    val selectedFilter: FilterType = FilterType.ALL,
    val selectedSessionSize: Int? = null, // null means all
    val settings: AppSettingsState = AppSettingsState(),
    val globalReviewedCount: Int = 0,
    val globalKeptCount: Int = 0,
    val globalPendingThrowCount: Int = 0,
    val globalBytesFreed: Long = 0L,
    val pastSessions: List<SessionEntity> = emptyList(),
    val pastBatches: List<RemovalBatchEntity> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val showOnboarding: Boolean = false,
    val showSettings: Boolean = false,
    val showMetadataSheet: Boolean = false,
    val showBatchConfirmDialog: Boolean = false,
    val pendingNativeIntent: PendingIntent? = null,
    val pendingNativeBatchId: String? = null,
    val pendingNativeItems: List<ItemWithAsset> = emptyList(),
    val sessionCompletedSummary: Boolean = false,
    val hasDeviceMediaPermission: Boolean = false
) {
    val currentItem: ItemWithAsset?
        get() = if (currentIndex in sessionItems.indices) sessionItems[currentIndex] else null

    val remainingCount: Int
        get() = (sessionItems.size - currentIndex).coerceAtLeast(0)

    val currentSessionEstimatedBytes: Long
        get() = sessionItems.filter { it.decision == DecisionType.THROW.name }.sumOf { it.sizeBytes }

    val totalPendingBytes: Long
        get() = pendingThrowItems.sumOf { it.sizeBytes }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = MediaRepository(application, db.mediaDao())
    private val settingsManager = AppSettingsManager(application)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var sessionItemsJob: Job? = null

    init {
        val initialSettings = settingsManager.getSettings()
        _uiState.update {
            it.copy(
                settings = initialSettings,
                showOnboarding = !initialSettings.onboardingCompleted
            )
        }

        observeDatabase()
        refreshAlbums()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.activeSessionFlow.collect { session ->
                _uiState.update { it.copy(activeSession = session) }
            }
        }
        viewModelScope.launch {
            repository.allActiveSessionsFlow.collect { sessions ->
                _uiState.update { it.copy(allActiveSessions = sessions) }
            }
        }
        viewModelScope.launch {
            repository.allPendingThrowFlow.collect { list ->
                _uiState.update { it.copy(pendingThrowItems = list) }
            }
        }
        viewModelScope.launch {
            repository.totalReviewedFlow.collect { count ->
                _uiState.update { it.copy(globalReviewedCount = count) }
            }
        }
        viewModelScope.launch {
            repository.totalKeptFlow.collect { count ->
                _uiState.update { it.copy(globalKeptCount = count) }
            }
        }
        viewModelScope.launch {
            repository.totalPendingThrowFlow.collect { count ->
                _uiState.update { it.copy(globalPendingThrowCount = count) }
            }
        }
        viewModelScope.launch {
            repository.totalBytesFreedFlow.collect { freed ->
                _uiState.update { it.copy(globalBytesFreed = freed) }
            }
        }
        viewModelScope.launch {
            repository.allSessionsFlow.collect { sessions ->
                _uiState.update { it.copy(pastSessions = sessions) }
            }
        }
        viewModelScope.launch {
            repository.allBatchesFlow.collect { batches ->
                _uiState.update { it.copy(pastBatches = batches) }
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasDeviceMediaPermission = granted) }
        if (granted) {
            refreshAlbums()
        }
    }

    fun setTab(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun toggleSettings(show: Boolean) {
        _uiState.update { it.copy(showSettings = show) }
    }

    fun toggleMetadataSheet(show: Boolean) {
        _uiState.update { it.copy(showMetadataSheet = show) }
    }

    fun toggleBatchConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showBatchConfirmDialog = show) }
    }

    fun completeOnboarding() {
        val updated = _uiState.value.settings.copy(onboardingCompleted = true)
        settingsManager.updateSettings(updated)
        _uiState.update { it.copy(settings = updated, showOnboarding = false) }
    }

    fun refreshAlbums() {
        viewModelScope.launch {
            val albumList = repository.getDeviceAlbums()
            _uiState.update { it.copy(albums = albumList) }
        }
    }

    fun selectAlbum(album: String?) {
        _uiState.update { it.copy(selectedAlbum = album) }
    }

    fun selectFilter(filter: FilterType) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun selectSessionSize(size: Int?) {
        _uiState.update { it.copy(selectedSessionSize = size) }
    }

    fun setSortOrder(order: MediaSortOrder) {
        val updated = _uiState.value.settings.copy(sortOrder = order)
        settingsManager.updateSettings(updated)
        _uiState.update { it.copy(settings = updated) }
        showSnackbar("Sort order: ${order.label}")
    }

    private var isProcessingDecision = false

    fun startDeviceSession(albumName: String? = _uiState.value.selectedAlbum) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val rawAssets = repository.queryDeviceMedia(
                albumName = albumName,
                filterType = _uiState.value.selectedFilter,
                excludeFavorites = _uiState.value.settings.protectFavorites,
                maxItems = _uiState.value.selectedSessionSize,
                sortOrder = _uiState.value.settings.sortOrder
            )

            if (rawAssets.isEmpty()) {
                _uiState.update { it.copy(isLoading = false) }
                showSnackbar("No media found for the selected filter or album.")
                return@launch
            }

            val title = if (albumName.isNullOrEmpty() || albumName == "All") "Phone Photos & Videos" else albumName
            val sessionId = repository.startNewSession(
                sourceId = MediaSourceInfo.DeviceLibrary.id,
                title = "$title (${rawAssets.size} items)",
                filterType = _uiState.value.selectedFilter,
                assets = rawAssets
            )
            _uiState.update { it.copy(isSorting = true) }
            loadAndOpenSession(sessionId)
        }
    }

    fun startSystemPickerSession(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val assets = repository.resolveSystemPickerAssets(uris)
            val sessionId = repository.startNewSession(
                sourceId = MediaSourceInfo.SystemPicker.id,
                title = "Picked Photos & Videos (${assets.size} items)",
                filterType = FilterType.ALL,
                assets = assets
            )
            _uiState.update { it.copy(isSorting = true) }
            loadAndOpenSession(sessionId)
        }
    }

    fun resumeSession(sessionId: String) {
        _uiState.update { it.copy(isSorting = true) }
        loadAndOpenSession(sessionId)
    }

    private fun loadAndOpenSession(sessionId: String) {
        sessionItemsJob?.cancel()
        sessionItemsJob = viewModelScope.launch {
            repository.getSessionItemsFlow(sessionId).collect { items ->
                val firstUnreviewed = items.indexOfFirst { it.decision == DecisionType.NONE.name }
                val targetIndex = if (firstUnreviewed >= 0) firstUnreviewed else items.size
                _uiState.update { state ->
                    val index = if (state.activeSessionId == sessionId && state.sessionItems.isNotEmpty()) {
                        state.currentIndex.coerceAtMost(items.size)
                    } else targetIndex

                    state.copy(
                        activeSessionId = sessionId,
                        sessionItems = items,
                        currentIndex = index,
                        isLoading = false,
                        sessionCompletedSummary = index >= items.size && items.isNotEmpty()
                    )
                }
            }
        }
    }

    fun makeDecision(decision: DecisionType) {
        val state = _uiState.value
        val sessionId = state.activeSessionId ?: return
        val item = state.currentItem ?: return
        if (isProcessingDecision) return
        isProcessingDecision = true

        triggerHaptic()

        val nextIndex = state.currentIndex + 1
        val isCompleted = nextIndex >= state.sessionItems.size

        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                sessionCompletedSummary = isCompleted
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.recordDecision(sessionId, item.assetId, decision)
            } finally {
                isProcessingDecision = false
            }
        }

        val decisionWord = when (decision) {
            DecisionType.KEEP -> "Kept"
            DecisionType.THROW -> "Marked for removal"
            DecisionType.SKIP -> "Skipped"
            DecisionType.NONE -> ""
        }
        if (decisionWord.isNotEmpty()) {
            showSnackbar("$decisionWord ${item.displayName}")
        }
    }

    fun pauseAndReturnHome() {
        val sessionId = _uiState.value.activeSessionId
        if (sessionId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.refreshSessionStats(sessionId)
            }
        }
        _uiState.update {
            it.copy(
                isSorting = false,
                sessionCompletedSummary = false,
                currentTab = MainTab.HOME
            )
        }
        showSnackbar("Session paused & progress saved. You can switch sources or resume anytime.")
    }

    fun destroySession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.activeSessionId == sessionId) {
                _uiState.update {
                    it.copy(
                        activeSessionId = null,
                        sessionItems = emptyList(),
                        currentIndex = 0,
                        isSorting = false,
                        sessionCompletedSummary = false
                    )
                }
            }
            showSnackbar("Session discarded. (Original media was untouched)")
        }
    }

    fun undoLastDecision() {
        val state = _uiState.value
        val sessionId = state.activeSessionId ?: return
        if (isProcessingDecision) return
        isProcessingDecision = true

        viewModelScope.launch {
            try {
                val undone = repository.undoLastDecision(sessionId)
                if (undone) {
                    triggerHaptic()
                    val prevIndex = (state.currentIndex - 1).coerceAtLeast(0)
                    _uiState.update {
                        it.copy(
                            currentIndex = prevIndex,
                            sessionCompletedSummary = false
                        )
                    }
                    showSnackbar("Undone last decision")
                } else {
                    showSnackbar("No decisions to undo")
                }
            } finally {
                isProcessingDecision = false
            }
        }
    }

    fun skipCurrent() {
        makeDecision(DecisionType.SKIP)
    }

    fun closeSorting() {
        _uiState.update {
            it.copy(
                isSorting = false,
                sessionCompletedSummary = false
            )
        }
    }

    fun changePendingItemDecision(item: ItemWithAsset, newDecision: DecisionType) {
        viewModelScope.launch {
            repository.changeItemDecision(item.sessionId, item.assetId, newDecision)
            triggerHaptic()
            showSnackbar("${item.displayName} changed to ${newDecision.name}")
        }
    }

    fun keepAllPendingRemovals() {
        val items = _uiState.value.pendingThrowItems
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { item ->
                repository.changeItemDecision(item.sessionId, item.assetId, DecisionType.KEEP)
            }
            triggerHaptic()
            showSnackbar("Restored all ${items.size} pending items to Keep")
        }
    }

    fun executePendingRemovalBatch() {
        val items = _uiState.value.pendingThrowItems
        if (items.isEmpty()) return

        val sourceId = items.first().sourceId
        val sessionId = items.first().sessionId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showBatchConfirmDialog = false) }
            val result = repository.processPendingBatch(sourceId, sessionId, items)
            when (result) {
                is DeletionResult.Success -> {
                    triggerHaptic()
                    showSnackbar("Successfully removed ${result.batch.requestedCount} items! Freed ${formatBytes(result.batch.bytesFreed)}")
                    _uiState.update { it.copy(isLoading = false) }
                }
                is DeletionResult.RequiresNativePermission -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingNativeIntent = result.intentSender,
                            pendingNativeBatchId = result.batchId,
                            pendingNativeItems = items
                        )
                    }
                }
                is DeletionResult.Partial -> {
                    showSnackbar("Partial removal: ${result.batch.succeededCount} removed, ${result.failedCount} failed.")
                    _uiState.update { it.copy(isLoading = false) }
                }
                is DeletionResult.ReviewOnlyNotice -> {
                    showSnackbar(result.message)
                    _uiState.update { it.copy(isLoading = false) }
                }
                is DeletionResult.Error -> {
                    showSnackbar("Error: ${result.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onNativePermissionResult(approved: Boolean) {
        val state = _uiState.value
        val batchId = state.pendingNativeBatchId ?: return
        val items = state.pendingNativeItems
        val sourceId = items.firstOrNull()?.sourceId ?: MediaSourceInfo.DeviceLibrary.id
        val sessionId = items.firstOrNull()?.sessionId

        viewModelScope.launch {
            repository.recordNativeBatchOutcome(batchId, sourceId, sessionId, items, approved)
            triggerHaptic()
            if (approved) {
                showSnackbar("Successfully moved ${items.size} items to System Trash!")
            } else {
                showSnackbar("Removal canceled. Review queue preserved.")
            }
            _uiState.update {
                it.copy(
                    pendingNativeIntent = null,
                    pendingNativeBatchId = null,
                    pendingNativeItems = emptyList()
                )
            }
        }
    }

    fun updateSettings(newSettings: AppSettingsState) {
        settingsManager.updateSettings(newSettings)
        _uiState.update { it.copy(settings = newSettings) }
        showSnackbar("Settings updated")
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            showSnackbar("Review history reset. (Original media was untouched)")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun triggerHaptic() {
        if (!_uiState.value.settings.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val mb = bytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", bytes / 1024.0)
        }
    }
}
