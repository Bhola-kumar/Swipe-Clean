package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SwipeScreen
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber900
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.KeepOrThrowTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.ThrowColor
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Android 13+ Photo Picker launcher (zero-permission)
    private val pickMultipleVisualMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.startSystemPickerSession(uris)
        }
    }

    // Runtime permission launcher for full device library access
    private val requestMediaPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { it }
        viewModel.setPermissionGranted(isGranted)
        if (isGranted) {
            viewModel.refreshAlbums()
            viewModel.startDeviceSession()
        } else {
            viewModel.showSnackbar("Media access not granted. You can use Photo Picker to select specific photos.")
        }
    }

    // Native IntentSender launcher for Android 11+ MediaStore.createTrashRequest
    private val nativeIntentSenderLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val approved = result.resultCode == Activity.RESULT_OK
        viewModel.onNativePermissionResult(approved)
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            KeepOrThrowTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                // Observe snackbar messages
                LaunchedEffect(uiState.snackbarMessage) {
                    uiState.snackbarMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearSnackbar()
                    }
                }

                // Launch native confirmation if requested
                LaunchedEffect(uiState.pendingNativeIntent) {
                    uiState.pendingNativeIntent?.let { pendingIntent ->
                        val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                        nativeIntentSenderLauncher.launch(request)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CreamBackground
                ) {
                    SharedTransitionLayout {
                        AnimatedContent(
                            targetState = when {
                                uiState.isSorting -> ScreenMode.SORTING
                                uiState.showSettings -> ScreenMode.SETTINGS
                                else -> ScreenMode.MAIN_SHELL
                            },
                            transitionSpec = {
                                if (targetState == ScreenMode.SORTING) {
                                    // Smooth slide-up & scale-in transition into the card stack
                                    (slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeIn() + scaleIn(initialScale = 0.94f)) togetherWith
                                    (fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.96f))
                                } else if (initialState == ScreenMode.SORTING) {
                                    // Smooth slide-down & scale-out exit from the card stack
                                    (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.96f)) togetherWith
                                    (slideOutVertically(
                                        targetOffsetY = { it / 3 },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeOut() + scaleOut(targetScale = 0.94f))
                                } else {
                                    // Settings or general transition
                                    (fadeIn(tween(200)) + scaleIn(initialScale = 0.97f)) togetherWith
                                    (fadeOut(tween(150)) + scaleOut(targetScale = 0.97f))
                                }
                            },
                            label = "screen_mode_transition"
                        ) { screenMode ->
                            when (screenMode) {
                                ScreenMode.SORTING -> {
                                    SwipeScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onNavigateToReview = {
                                            viewModel.closeSorting()
                                            viewModel.setTab(MainTab.REVIEW)
                                        }
                                    )
                                }
                                ScreenMode.SETTINGS -> {
                                    SettingsScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onClose = { viewModel.toggleSettings(false) }
                                    )
                                }
                                ScreenMode.MAIN_SHELL -> {
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize(),
                                        contentWindowInsets = WindowInsets.safeDrawing,
                                        containerColor = CreamBackground,
                                        snackbarHost = { SnackbarHost(snackbarHostState) },
                                        bottomBar = {
                                            BottomNavigationBar(
                                                currentTab = uiState.currentTab,
                                                pendingThrowCount = uiState.pendingThrowItems.size,
                                                onSelectTab = { viewModel.setTab(it) }
                                            )
                                        }
                                    ) { innerPadding ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(innerPadding)
                                        ) {
                                            AnimatedContent(
                                                targetState = uiState.currentTab,
                                                transitionSpec = {
                                                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                                    (slideInHorizontally(
                                                        initialOffsetX = { direction * it / 5 },
                                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                                    ) + fadeIn(tween(200))) togetherWith
                                                    (slideOutHorizontally(
                                                        targetOffsetX = { -direction * it / 5 },
                                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                                                    ) + fadeOut(tween(150)))
                                                },
                                                label = "tab_transition"
                                            ) { tab ->
                                                when (tab) {
                                                    MainTab.HOME -> HomeScreen(
                                                        uiState = uiState,
                                                        viewModel = viewModel,
                                                        onRequestDeviceMedia = { requestDeviceAccess() },
                                                        onLaunchSystemPicker = {
                                                            pickMultipleVisualMediaLauncher.launch(
                                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                                            )
                                                        }
                                                    )
                                                    MainTab.EXPLORE -> ExploreScreen(
                                                        uiState = uiState,
                                                        viewModel = viewModel,
                                                        onRequestDeviceScan = { requestDeviceAccess() }
                                                    )
                                                    MainTab.REVIEW -> ReviewScreen(
                                                        uiState = uiState,
                                                        viewModel = viewModel
                                                    )
                                                    MainTab.HISTORY -> HistoryScreen(
                                                        uiState = uiState,
                                                        viewModel = viewModel
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Onboarding Dialog
                    if (uiState.showOnboarding) {
                        OnboardingDialog(
                            onDismiss = {
                                viewModel.completeOnboarding()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestDeviceAccess() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestMediaPermissionsLauncher.launch(permissions)
    }
}

enum class ScreenMode {
    MAIN_SHELL,
    SORTING,
    SETTINGS
}

@Composable
private fun BottomNavigationBar(
    currentTab: MainTab,
    pendingThrowCount: Int,
    onSelectTab: (MainTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = PureWhite,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentTab == MainTab.HOME,
            onClick = { onSelectTab(MainTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = if (currentTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_home"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Amber900,
                selectedTextColor = Amber900,
                indicatorColor = Amber100,
                unselectedIconColor = TextDarkSecondary,
                unselectedTextColor = TextDarkSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == MainTab.EXPLORE,
            onClick = { onSelectTab(MainTab.EXPLORE) },
            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
            label = { Text("Explore", fontWeight = if (currentTab == MainTab.EXPLORE) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_explore"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Amber900,
                selectedTextColor = Amber900,
                indicatorColor = Amber100,
                unselectedIconColor = TextDarkSecondary,
                unselectedTextColor = TextDarkSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == MainTab.REVIEW,
            onClick = { onSelectTab(MainTab.REVIEW) },
            icon = {
                if (pendingThrowCount > 0) {
                    BadgedBox(badge = {
                        Badge(containerColor = ThrowColor) {
                            Text(pendingThrowCount.toString(), color = Color.White)
                        }
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Review")
                    }
                } else {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Review")
                }
            },
            label = { Text("Review", fontWeight = if (currentTab == MainTab.REVIEW) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_review"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Amber900,
                selectedTextColor = Amber900,
                indicatorColor = Amber100,
                unselectedIconColor = TextDarkSecondary,
                unselectedTextColor = TextDarkSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == MainTab.HISTORY,
            onClick = { onSelectTab(MainTab.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History", fontWeight = if (currentTab == MainTab.HISTORY) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_history"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Amber900,
                selectedTextColor = Amber900,
                indicatorColor = Amber100,
                unselectedIconColor = TextDarkSecondary,
                unselectedTextColor = TextDarkSecondary
            )
        )
    }
}
