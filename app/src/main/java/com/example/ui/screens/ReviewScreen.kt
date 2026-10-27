package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.DecisionType
import com.example.data.ItemWithAsset
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber900
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.KeepEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.StoneVariant
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.ThrowColor
import com.example.ui.theme.ThrowContainerLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun ReviewScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val pendingItems = uiState.pendingThrowItems
    val totalPending = pendingItems.size
    val totalBytes = uiState.totalPendingBytes

    var filterType by remember { mutableStateOf("ALL") }

    val filteredItems = remember(pendingItems, filterType) {
        when (filterType) {
            "PHOTOS" -> pendingItems.filter { !it.isVideo }
            "VIDEOS" -> pendingItems.filter { it.isVideo }
            else -> pendingItems
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .testTag("review_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Review Removals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary
                )
                Text(
                    text = "Verify decisions before anything is deleted",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDarkSecondary
                )
            }

            if (totalPending > 0) {
                OutlinedButton(
                    onClick = { viewModel.keepAllPendingRemovals() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("keep_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = KeepEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Keep All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KeepEmerald)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (totalPending == 0) {
            // Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Amber100,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Review Queue is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "No media marked for removal. Start a sorting session and swipe left on items you want to discard.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalPending items marked for removal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                        Text(
                            text = "Estimated pending space: ${formatBytes(totalBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ThrowColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = ThrowContainerLight,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = ThrowColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Amber600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Nothing is deleted yet. Tapping removal will ask for official system confirmation.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDarkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of pending items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredItems, key = { it.assetId }) { item ->
                    PendingItemCard(
                        item = item,
                        onKeepInstead = {
                            viewModel.changePendingItemDecision(item, DecisionType.KEEP)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Execute Button
            Button(
                onClick = { viewModel.toggleBatchConfirmDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = ThrowColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("execute_removal_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review Removal of $totalPending Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Confirmation Modal
    if (uiState.showBatchConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleBatchConfirmDialog(false) },
            containerColor = PureWhite,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Amber600,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Removal",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = "You are about to discard $totalPending items (${formatBytes(totalBytes)}).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Local Originals: Items are moved to your device Gallery Trash for 30 days.\n• Cloud Sync: If gallery is synced to Google Photos/OneDrive, changes may sync.\n• Native Confirmation: Android will display an official confirmation dialog.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executePendingRemovalBatch() },
                    colors = ButtonDefaults.buttonColors(containerColor = ThrowColor),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Proceed to Native Approval", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleBatchConfirmDialog(false) }) {
                    Text("Cancel (Keep in Queue)", color = TextDarkSecondary)
                }
            }
        )
    }
}

@Composable
private fun PendingItemCard(
    item: ItemWithAsset,
    onKeepInstead: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.uriString)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = CircleShape,
                    color = ThrowColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatBytes(item.sizeBytes),
                style = MaterialTheme.typography.labelSmall,
                color = TextDarkSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onKeepInstead,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("keep_instead_${item.assetId}")
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = KeepEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Keep instead",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeepEmerald
                )
            }
        }
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
