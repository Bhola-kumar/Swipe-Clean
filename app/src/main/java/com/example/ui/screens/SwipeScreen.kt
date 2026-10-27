package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DecisionType
import com.example.data.ItemWithAsset
import com.example.ui.components.ActionCelebrationOverlay
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.MediaCard
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber900
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.KeepContainerLight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    onNavigateToReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.pauseAndReturnHome()
    }

    val currentItem = uiState.currentItem
    val items = uiState.sessionItems
    val currentIndex = uiState.currentIndex
    val totalCount = items.size
    val rawProgress = if (totalCount > 0) currentIndex.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "progress"
    )

    val sheetState = rememberModalBottomSheetState()
    var isUiVisible by remember { mutableStateOf(false) }
    var actionEffect by remember { mutableStateOf<DecisionType?>(null) }

    val onDecisionWithEffect: (DecisionType) -> Unit = { decision ->
        actionEffect = decision
        viewModel.makeDecision(decision)
    }

    LaunchedEffect(Unit) {
        isUiVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Top Bar with Pause action
            AnimatedVisibility(
                visible = isUiVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.pauseAndReturnHome() },
                        modifier = Modifier.testTag("pause_sorting_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = "Pause Session & Switch Source",
                            tint = Amber600,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = uiState.activeSession?.title ?: "Sorting Session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        AnimatedContent(
                            targetState = currentIndex,
                            transitionSpec = {
                                (slideInVertically { it / 2 } + fadeIn()) togetherWith
                                (slideOutVertically { -it / 2 } + fadeOut())
                            },
                            label = "counter_anim"
                        ) { targetIndex ->
                            Text(
                                text = if (totalCount > 0) "${targetIndex.coerceAtMost(totalCount)} of $totalCount" else "0 items",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleMetadataSheet(true) },
                        enabled = currentItem != null,
                        modifier = Modifier.testTag("info_metadata_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Item Metadata",
                            tint = if (currentItem != null) Amber600 else Color.Gray
                        )
                    }
                }
            }

            // Amber Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Amber500,
                trackColor = Amber100
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card Stack Area: Rendered directly without double-animation wrappers for 100% butter smoothness
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.sessionCompletedSummary || currentItem == null) {
                    this@Column.AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                    ) {
                        SessionCompletedCard(
                            items = items,
                            onReview = onNavigateToReview,
                            onDone = { viewModel.closeSorting() }
                        )
                    }
                } else {
                    // Peek cards in the background
                    val nextIndex1 = currentIndex + 2
                    val nextIndex2 = currentIndex + 1

                    if (nextIndex1 < items.size) {
                        key(items[nextIndex1].assetId) {
                            MediaCard(
                                item = items[nextIndex1],
                                swipeConfig = uiState.settings.swipeDirection,
                                isTopCard = false,
                                settings = uiState.settings,
                                onDecision = {},
                                modifier = Modifier
                                    .scale(0.88f)
                                    .padding(top = 24.dp)
                            )
                        }
                    }

                    if (nextIndex2 < items.size) {
                        key(items[nextIndex2].assetId) {
                            MediaCard(
                                item = items[nextIndex2],
                                swipeConfig = uiState.settings.swipeDirection,
                                isTopCard = false,
                                settings = uiState.settings,
                                onDecision = {},
                                modifier = Modifier
                                    .scale(0.94f)
                                    .padding(top = 12.dp)
                            )
                        }
                    }

                    // Foreground active card: Keyed directly by assetId so transition from card to card is instant and fluid
                    key(currentItem.assetId) {
                        MediaCard(
                            item = currentItem,
                            swipeConfig = uiState.settings.swipeDirection,
                            isTopCard = true,
                            settings = uiState.settings,
                            onDecision = { decision -> onDecisionWithEffect(decision) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Action celebration / destruction overlay on top of the card stack
                ActionCelebrationOverlay(
                    decision = actionEffect,
                    onFinished = { actionEffect = null },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata summary chip with animated text
            AnimatedVisibility(
                visible = currentItem != null && !uiState.sessionCompletedSummary,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                if (currentItem != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentItem.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextDarkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatSize(currentItem.sizeBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = Amber900,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar with smooth slide-in entry
            AnimatedVisibility(
                visible = isUiVisible && !uiState.sessionCompletedSummary && currentItem != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn()
            ) {
                ActionButtonsBar(
                    isLeftHanded = uiState.settings.leftHandedMode,
                    canUndo = currentIndex > 0,
                    onThrow = { onDecisionWithEffect(DecisionType.THROW) },
                    onKeep = { onDecisionWithEffect(DecisionType.KEEP) },
                    onUndo = { viewModel.undoLastDecision() },
                    onSkip = { onDecisionWithEffect(DecisionType.SKIP) }
                )
            }
        }

        // Item Metadata Sheet
        if (uiState.showMetadataSheet && currentItem != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleMetadataSheet(false) },
                sheetState = sheetState,
                containerColor = PureWhite
            ) {
                MetadataSheetContent(
                    item = currentItem,
                    onClose = { viewModel.toggleMetadataSheet(false) }
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsBar(
    isLeftHanded: Boolean,
    canUndo: Boolean,
    onThrow: () -> Unit,
    onKeep: () -> Unit,
    onUndo: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isLeftHanded) {
            ThrowButton(onThrow)
            UndoButton(canUndo, onUndo)
            SkipButton(onSkip)
            KeepButton(onKeep)
        } else {
            KeepButton(onKeep)
            SkipButton(onSkip)
            UndoButton(canUndo, onUndo)
            ThrowButton(onThrow)
        }
    }
}

@Composable
private fun ThrowButton(onThrow: () -> Unit) {
    FilledTonalIconButton(
        onClick = onThrow,
        modifier = Modifier
            .size(62.dp)
            .testTag("throw_button"),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = ThrowContainerLight,
            contentColor = ThrowColor
        ),
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Review for removal (Throw)",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun KeepButton(onKeep: () -> Unit) {
    FilledTonalIconButton(
        onClick = onKeep,
        modifier = Modifier
            .size(62.dp)
            .testTag("keep_button"),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = KeepContainerLight,
            contentColor = KeepEmerald
        ),
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Keep this media",
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun UndoButton(canUndo: Boolean, onUndo: () -> Unit) {
    FilledTonalIconButton(
        onClick = onUndo,
        enabled = canUndo,
        modifier = Modifier
            .size(48.dp)
            .testTag("undo_button"),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = StoneVariant,
            contentColor = if (canUndo) TextDarkPrimary else Color.Gray
        ),
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = "Undo last decision",
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SkipButton(onSkip: () -> Unit) {
    FilledTonalIconButton(
        onClick = onSkip,
        modifier = Modifier
            .size(48.dp)
            .testTag("skip_button"),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = StoneVariant,
            contentColor = TextDarkPrimary
        ),
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Skip for later",
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SessionCompletedCard(
    items: List<ItemWithAsset>,
    onReview: () -> Unit,
    onDone: () -> Unit
) {
    val keptCount = items.count { it.decision == DecisionType.KEEP.name }
    val throwCount = items.count { it.decision == DecisionType.THROW.name }
    val skipCount = items.count { it.decision == DecisionType.SKIP.name }
    val bytesToFree = items.filter { it.decision == DecisionType.THROW.name }.sumOf { it.sizeBytes }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ConfettiCelebration(modifier = Modifier.fillMaxSize())

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Amber100,
                    modifier = Modifier.size(72.dp)
                ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Amber600,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Session Completed!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )

            Text(
                text = if (skipCount > 0) "All items reviewed ($skipCount skipped for later)." else "All items in this session have been reviewed.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Summary Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem("Kept", keptCount.toString(), KeepEmerald)
                SummaryStatItem("Pending", throwCount.toString(), ThrowColor)
                SummaryStatItem("Skipped", skipCount.toString(), Color.Gray)
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (throwCount > 0) {
                Text(
                    text = "Estimated pending space: ${formatBytes(bytesToFree)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ThrowColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onReview,
                    colors = ButtonDefaults.buttonColors(containerColor = ThrowColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("review_pending_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Review Removals ($throwCount)", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("done_session_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Home", color = TextDarkPrimary)
            }
        }
    }
}
}

@Composable
private fun SummaryStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextDarkSecondary
        )
    }
}

@Composable
private fun MetadataSheetContent(item: ItemWithAsset, onClose: () -> Unit) {
    val dateStr = if (item.dateAdded > 0) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(item.dateAdded * 1000))
    } else "Unavailable"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Item Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        DetailRow("File Name", item.displayName)
        DetailRow("Type", if (item.isVideo) "Video (${item.mimeType})" else "Photo (${item.mimeType})")
        if (item.isVideo && item.durationMs > 0) {
            DetailRow("Duration", "${item.durationMs / 1000}s")
        }
        DetailRow("File Size", formatBytes(item.sizeBytes))
        if (item.width > 0 && item.height > 0) {
            DetailRow("Dimensions", "${item.width} x ${item.height} px")
        }
        DetailRow("Date Added", dateStr)
        DetailRow("Album", item.albumName)
        DetailRow("Source Provider", item.sourceId)

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StoneVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔒 Privacy: All operations and metadata remain strictly on-device in local storage. Nothing is uploaded.",
                style = MaterialTheme.typography.bodySmall,
                color = TextDarkSecondary,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done", color = Color.White)
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextDarkSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextDarkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "Size unavailable"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.0f KB", bytes / 1024.0)
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
