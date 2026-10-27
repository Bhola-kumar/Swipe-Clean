package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.SessionEntity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FilterType
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
import com.example.ui.theme.ThrowColor
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    onRequestDeviceMedia: () -> Unit,
    onLaunchSystemPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sessionToDestroy by remember { mutableStateOf<SessionEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Swipe Clean",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                    Text(
                        text = "Clean photos & videos • 100% On-Device",
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber700,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleSettings(true) },
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextDarkPrimary
                    )
                }
            }
        }

        // Primary Action Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Amber100,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Start Cleaning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPrimary
                            )
                            Text(
                                text = "Review one by one • Swipe right to keep, left to trash",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRequestDeviceMedia,
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("start_sorting_button")
                        ) {
                            Text("Scan Gallery", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onLaunchSystemPicker,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Photo Picker", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Honest Local Statistics
        item {
            Text(
                text = "Local Storage Statistics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatBox("Reviewed", uiState.globalReviewedCount.toString(), Amber600)
                    StatBox("Kept", uiState.globalKeptCount.toString(), KeepEmerald)
                    StatBox("Pending", uiState.globalPendingThrowCount.toString(), ThrowColor)
                    StatBox("Freed Space", formatFreedBytes(uiState.globalBytesFreed), Amber700)
                }
            }
        }

        // Real Media Sources Section
        item {
            Text(
                text = "Select Media Source",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SourceCard(
                    title = "Phone Photos & Videos",
                    subtitle = "Scan device camera roll & folders • Uses system trash confirmation",
                    tag = "DEVICE MEDIA",
                    icon = Icons.Default.Collections,
                    iconTint = Amber600,
                    onClick = onRequestDeviceMedia
                )

                SourceCard(
                    title = "System Photo Picker",
                    subtitle = "Zero permissions required • Select individual items from device",
                    tag = "ZERO PERMISSIONS",
                    icon = Icons.Default.PhotoLibrary,
                    iconTint = KeepEmerald,
                    onClick = onLaunchSystemPicker
                )

                SourceCard(
                    title = "Google Photos Selection",
                    subtitle = "Cloud selection review • Marked items logged for manual removal",
                    tag = "REVIEW ONLY",
                    icon = Icons.Default.CloudQueue,
                    iconTint = Amber700,
                    onClick = onLaunchSystemPicker
                )
            }
        }

        // Quick Filter Shortcuts
        item {
            Text(
                text = "Quick Filters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShortcutChip(
                    label = "Screenshots",
                    icon = Icons.Default.ScreenSearchDesktop,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectFilter(FilterType.SCREENSHOTS)
                        viewModel.startDeviceSession()
                    }
                )

                ShortcutChip(
                    label = "Large Items",
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectFilter(FilterType.LARGE_ITEMS)
                        viewModel.startDeviceSession()
                    }
                )

                ShortcutChip(
                    label = "Videos",
                    icon = Icons.Default.Videocam,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectFilter(FilterType.VIDEOS_ONLY)
                        viewModel.startDeviceSession()
                    }
                )
            }
        }

        // Active & Paused Sessions Horizontal Carousel (Bottom)
        if (uiState.allActiveSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active & Paused Sessions (${uiState.allActiveSessions.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                        Text(
                            text = "Swipe horizontally to resume or discard",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber700
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sessions_carousel")
                ) {
                    items(uiState.allActiveSessions, key = { it.id }) { session ->
                        val total = session.totalCount
                        val reviewed = session.reviewedCount
                        val remaining = (total - reviewed).coerceAtLeast(0)
                        val progress = if (total > 0) reviewed.toFloat() / total else 0f

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Amber100),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Amber200),
                            modifier = Modifier
                                .width(285.dp)
                                .testTag("continue_session_card_${session.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Amber200
                                    ) {
                                        Text(
                                            text = session.filterType.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Amber900,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = PureWhite
                                    ) {
                                        Text(
                                            text = "$remaining left",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Amber900,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Amber600,
                                    trackColor = PureWhite
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$reviewed/$total (${(progress * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextDarkSecondary
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Discard / Destroy Session button
                                        IconButton(
                                            onClick = { sessionToDestroy = session },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("destroy_session_button_${session.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Discard Session",
                                                tint = ThrowColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Resume button
                                        Button(
                                            onClick = { viewModel.resumeSession(session.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("resume_session_button_${session.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Resume",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for discarding a session
    sessionToDestroy?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDestroy = null },
            title = {
                Text(
                    text = "Discard Session?",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary
                )
            },
            text = {
                Text(
                    text = "This will discard your sorting progress for \"${session.title}\". Your original photos and videos remain completely safe on your device.",
                    color = TextDarkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.destroySession(session.id)
                        sessionToDestroy = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThrowColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Discard Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDestroy = null }) {
                    Text("Cancel", color = TextDarkPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextDarkSecondary
        )
    }
}

@Composable
private fun SourceCard(
    title: String,
    subtitle: String,
    tag: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Amber100
                    ) {
                        Text(
                            text = tag,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber900,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDarkSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Amber600,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ShortcutChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Amber600,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextDarkPrimary
            )
        }
    }
}

private fun formatFreedBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.1f GB", gb)
        else -> String.format("%.0f MB", mb)
    }
}
