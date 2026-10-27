package com.example.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.AppSettingsState
import com.example.ui.theme.Amber500
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerComponent(
    uriString: String,
    isTopCard: Boolean,
    settings: AppSettingsState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember(settings.muteVideosByDefault) { mutableStateOf(settings.muteVideosByDefault) }
    var userManuallyPaused by remember(settings.autoplayVideos) { mutableStateOf(!settings.autoplayVideos) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }

    // Initialize ExoPlayer safely
    val exoPlayer = remember(uriString) {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            val mediaItem = MediaItem.fromUri(Uri.parse(uriString))
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_OFF
            volume = if (settings.muteVideosByDefault) 0f else 1f
            prepare()
            if (settings.autoplayVideos && isTopCard) {
                playWhenReady = true
            }
        }
    }

    // Monitor ExoPlayer state & errors
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isLoading = true
                    }
                    Player.STATE_READY -> {
                        isLoading = false
                        errorMessage = null
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    }
                    Player.STATE_ENDED -> {
                        isLoading = false
                        isPlaying = false
                    }
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    isLoading = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isLoading = false
                errorMessage = error.localizedMessage ?: "Unable to play video format"
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Lifecycle listener to handle background / foreground pause & resume
    DisposableEffect(lifecycleOwner, isTopCard) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isTopCard && !userManuallyPaused && errorMessage == null) {
                        exoPlayer.playWhenReady = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!isTopCard) {
                exoPlayer.pause()
            }
        }
    }

    // Update playWhenReady when isTopCard changes
    LaunchedEffect(isTopCard) {
        if (!isTopCard) {
            exoPlayer.pause()
        } else if (settings.autoplayVideos && !userManuallyPaused && errorMessage == null) {
            exoPlayer.playWhenReady = true
        }
    }

    // Continuous position tracking loop
    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(250)
        }
    }

    // Sync volume with muted state
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Native ExoPlayer View with Aspect Ratio FIT
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom Compose controller overlay
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Feedback Spinner
        if (isLoading && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Amber500,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Error State UI
        errorMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Video Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Video Playback Error",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }

        // Playback Controls Overlay
        AnimatedVisibility(
            visible = showControls && isTopCard && errorMessage == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                // Center Big Play/Pause/Replay Button
                IconButton(
                    onClick = {
                        if (exoPlayer.playbackState == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = true
                            userManuallyPaused = false
                        } else if (isPlaying) {
                            exoPlayer.pause()
                            userManuallyPaused = true
                        } else {
                            exoPlayer.playWhenReady = true
                            userManuallyPaused = false
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .testTag("video_play_pause_button"),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = when {
                            exoPlayer.playbackState == Player.STATE_ENDED -> Icons.Default.Replay
                            isPlaying -> Icons.Default.Pause
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = PureWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Top Right Mute/Unmute Toggle
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.testTag("video_mute_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Bottom Seek Slider & Timestamp Bar
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDuration(currentPositionMs),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatDuration(durationMs),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        if (durationMs > 0) {
                            Slider(
                                value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                                onValueChange = { targetPos ->
                                    currentPositionMs = targetPos.toLong()
                                },
                                onValueChangeFinished = {
                                    exoPlayer.seekTo(currentPositionMs)
                                },
                                valueRange = 0f..durationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Amber500,
                                    activeTrackColor = Amber500,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .testTag("video_seek_bar")
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    val hours = (ms / 3600000)
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
