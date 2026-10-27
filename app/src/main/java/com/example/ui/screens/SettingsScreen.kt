package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppThemeMode
import com.example.data.MediaSortOrder
import com.example.data.MediaSourceInfo
import com.example.data.SwipeDirectionConfig
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber200
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber700
import com.example.ui.theme.Amber900
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.KeepEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.StoneVariant
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCapabilitiesSheet by remember { mutableStateOf(false) }
    var showPrivacyPolicySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val settings = uiState.settings

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preferences & Storage",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )

            IconButton(onClick = onClose, modifier = Modifier.testTag("close_settings_button")) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Settings", tint = TextDarkPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Gestures Section
            item {
                SettingsSectionHeader("Gestures & Feedback")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Swipe Direction",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSettings(settings.copy(swipeDirection = SwipeDirectionConfig.NORMAL))
                                }
                        ) {
                            RadioButton(
                                selected = settings.swipeDirection == SwipeDirectionConfig.NORMAL,
                                onClick = { viewModel.updateSettings(settings.copy(swipeDirection = SwipeDirectionConfig.NORMAL)) },
                                colors = RadioButtonDefaults.colors(selectedColor = Amber600)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Right: Keep • Left: Throw", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDarkPrimary)
                                Text("Default natural gesture convention", fontSize = 12.sp, color = TextDarkSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSettings(settings.copy(swipeDirection = SwipeDirectionConfig.INVERTED))
                                }
                        ) {
                            RadioButton(
                                selected = settings.swipeDirection == SwipeDirectionConfig.INVERTED,
                                onClick = { viewModel.updateSettings(settings.copy(swipeDirection = SwipeDirectionConfig.INVERTED)) },
                                colors = RadioButtonDefaults.colors(selectedColor = Amber600)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Right: Throw • Left: Keep", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDarkPrimary)
                                Text("Inverted direction preference", fontSize = 12.sp, color = TextDarkSecondary)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StoneBorder)

                        // Haptics switch
                        SettingsSwitchRow(
                            title = "Haptic Vibration",
                            subtitle = "Vibrate on committed swipe and undo actions",
                            checked = settings.hapticsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(hapticsEnabled = it)) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StoneBorder)

                        // Left-Handed mode switch
                        SettingsSwitchRow(
                            title = "Left-Handed Action Bar",
                            subtitle = "Places Keep button on the left for one-handed thumb comfort",
                            checked = settings.leftHandedMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(leftHandedMode = it)) }
                        )
                    }
                }
            }

            // Media Flow & Sort Order
            item {
                SettingsSectionHeader("Media Flow & Sort Order")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Item Sequence",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                        Text(
                            text = "Preserves the natural camera roll timeline (photos and videos interleaved as taken) or custom order",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        MediaSortOrder.values().forEach { order ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSortOrder(order)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = settings.sortOrder == order,
                                    onClick = { viewModel.setSortOrder(order) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Amber600)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = order.label,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextDarkPrimary
                                    )
                                    val desc = when (order) {
                                        MediaSortOrder.NEWEST_FIRST -> "Natural camera timeline (photos & videos in chronological order of capture)"
                                        MediaSortOrder.OLDEST_FIRST -> "Oldest items first (clean out old archives & earliest memories)"
                                        MediaSortOrder.LARGEST_FIRST -> "Largest size first (fastest way to reclaim large storage space)"
                                        MediaSortOrder.SHUFFLE -> "Randomized mix of photos and videos"
                                    }
                                    Text(desc, fontSize = 12.sp, color = TextDarkSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Media & Protection
            item {
                SettingsSectionHeader("Media Behavior")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsSwitchRow(
                            title = "Exclude Favorites by Default",
                            subtitle = "Shield starred photos from accidental removal queues",
                            checked = settings.protectFavorites,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(protectFavorites = it)) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StoneBorder)

                        SettingsSwitchRow(
                            title = "Autoplay Videos",
                            subtitle = "Automatically start video playback when a card becomes active",
                            checked = settings.autoplayVideos,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(autoplayVideos = it)) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = StoneBorder)

                        SettingsSwitchRow(
                            title = "Play Videos with Audio Enabled",
                            subtitle = "Plays video sound by default instead of starting muted",
                            checked = !settings.muteVideosByDefault,
                            onCheckedChange = { audioEnabled ->
                                viewModel.updateSettings(
                                    settings.copy(
                                        muteVideosByDefault = !audioEnabled,
                                        soundEnabled = audioEnabled
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Source Capabilities Matrix
            item {
                SettingsSectionHeader("Platform Capabilities")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCapabilitiesSheet = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Source Capability Matrix",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                            Text(
                                text = "Learn how phone media, picker, and Google Photos differ",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary
                            )
                        }
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Amber600)
                    }
                }
            }

            // Privacy Manifesto & In-App Policy
            item {
                SettingsSectionHeader("Local Storage & Privacy Guarantee")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Amber100.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Amber700)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("100% On-Device Local Storage", fontWeight = FontWeight.Bold, color = Amber900)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Swipe Clean never sends your photos, filenames, thumbnails, or location metadata to any external server. All sorting, session states, and decision logs are stored securely in local SQLite (Room) on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Amber200,
                            modifier = Modifier.clickable { showPrivacyPolicySheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Amber900, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Full Privacy Policy & Compliance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber900)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCapabilitiesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCapabilitiesSheet = false },
            sheetState = sheetState,
            containerColor = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Source Capability Matrix",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val sources = listOf(
                    MediaSourceInfo.DeviceLibrary,
                    MediaSourceInfo.SystemPicker,
                    MediaSourceInfo.GooglePhotos
                )

                sources.forEach { source ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StoneVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(source.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextDarkPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Deletion scope: ${source.capabilities.deletionScopeNotice}", fontSize = 12.sp, color = TextDarkSecondary)
                            Text("• Recovery: ${source.capabilities.recoveryDescription}", fontSize = 12.sp, color = TextDarkSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showPrivacyPolicySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacyPolicySheet = false },
            sheetState = sheetState,
            containerColor = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Privacy Policy & Compliance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Effective Date: October 2026 • Version ${com.example.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Amber700,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(350.dp)
                ) {
                    item {
                        Text(
                            text = "1. Zero Cloud Collection\nSwipe Clean operates 100% locally on your Android device. We do not collect, transmit, upload, or share your photos, videos, filenames, file locations, or usage analytics with any external servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                    }
                    item {
                        Text(
                            text = "2. Device Media Permissions\nThe app accesses your device storage solely to display media items inside sorting sessions and execute user-approved deletions via official Android system APIs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                    }
                    item {
                        Text(
                            text = "3. On-Device Storage (Room Database)\nAll sorting decisions, session states, and undo history entries are stored exclusively in an encrypted, sandboxed local Room SQLite database on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                    }
                    item {
                        Text(
                            text = "4. No Ads or Third-Party Trackers\nThis application contains no advertisements, no tracking SDKs, no behavioral profiling tools, and no telemetry libraries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                    }
                    item {
                        Text(
                            text = "5. User Control & Data Erasure\nYou can delete any sorting session or clear local app data at any time from device app settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Amber700,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextDarkPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Amber600,
                checkedTrackColor = Amber100
            )
        )
    }
}
