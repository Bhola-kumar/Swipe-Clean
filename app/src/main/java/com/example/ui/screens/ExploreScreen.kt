package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.AlbumInfo
import com.example.data.FilterType
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber200
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Amber700
import com.example.ui.theme.Amber900
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.StoneVariant
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun ExploreScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    onRequestDeviceScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .testTag("explore_screen")
    ) {
        Text(
            text = "Explore & Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextDarkPrimary
        )
        Text(
            text = "Filter albums, media types, and session sizes",
            style = MaterialTheme.typography.bodySmall,
            color = TextDarkSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Type Row
        Text(
            text = "Media Type Filter",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Amber900
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            val filters = listOf(
                FilterType.ALL to "All Media",
                FilterType.PHOTOS_ONLY to "Photos Only",
                FilterType.VIDEOS_ONLY to "Videos Only",
                FilterType.SCREENSHOTS to "Screenshots",
                FilterType.LARGE_ITEMS to "Large Files (>5MB)"
            )
            items(filters) { (type, label) ->
                FilterChip(
                    selected = uiState.selectedFilter == type,
                    onClick = { viewModel.selectFilter(type) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Amber100,
                        selectedLabelColor = Amber900
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.selectedFilter == type,
                        borderColor = if (uiState.selectedFilter == type) Amber500 else StoneBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Session Size Row
        Text(
            text = "Session Size",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Amber900
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sizes = listOf(
                20 to "Quick (20)",
                50 to "Focused (50)",
                null to "Deep Dive (All)"
            )
            sizes.forEach { (size, label) ->
                FilterChip(
                    selected = uiState.selectedSessionSize == size,
                    onClick = { viewModel.selectSessionSize(size) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Amber100,
                        selectedLabelColor = Amber900
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.selectedSessionSize == size,
                        borderColor = if (uiState.selectedSessionSize == size) Amber500 else StoneBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Albums Grid Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Device Albums & Folders",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary
            )
            Text(
                text = "${uiState.albums.size} folders found",
                style = MaterialTheme.typography.labelSmall,
                color = TextDarkSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.albums.isEmpty()) {
            // Empty state for albums
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Amber100,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Albums Scanned Yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDarkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan device storage to group your photos by Camera, Screenshots, WhatsApp, and more.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRequestDeviceScan,
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Scan Device Media", color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Albums Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.albums) { album ->
                    AlbumCard(
                        album = album,
                        isSelected = uiState.selectedAlbum == album.name,
                        onClick = {
                            viewModel.selectAlbum(if (uiState.selectedAlbum == album.name) null else album.name)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Launch Button
        Button(
            onClick = {
                viewModel.startDeviceSession(uiState.selectedAlbum)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("start_explore_session_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.selectedAlbum != null) "Start Sorting ${uiState.selectedAlbum}" else "Start Sorting Selected Filters",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AlbumCard(
    album: AlbumInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Amber100 else PureWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Amber500 else StoneBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StoneVariant),
                contentAlignment = Alignment.Center
            ) {
                if (album.coverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.coverUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (album.name.contains("video", ignoreCase = true)) Icons.Default.Videocam else Icons.Default.Folder,
                        contentDescription = null,
                        tint = Amber600,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = album.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${album.totalCount} items (${album.photoCount} photos, ${album.videoCount} videos)",
                style = MaterialTheme.typography.labelSmall,
                color = TextDarkSecondary,
                fontSize = 11.sp
            )
        }
    }
}
