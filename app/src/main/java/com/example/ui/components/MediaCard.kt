package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AppSettingsState
import com.example.data.DecisionType
import com.example.data.ItemWithAsset
import com.example.data.SwipeDirectionConfig
import com.example.ui.theme.Amber500
import com.example.ui.theme.KeepEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.ThrowColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MediaCard(
    item: ItemWithAsset,
    swipeConfig: SwipeDirectionConfig,
    isTopCard: Boolean,
    settings: AppSettingsState = AppSettingsState(),
    onDecision: (DecisionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thresholdPx = remember(density) { with(density) { 140.dp.toPx() } }

    val animOffsetX = remember(item.assetId) { Animatable(0f) }
    var zoomScale by remember(item.assetId) { mutableFloatStateOf(1f) }
    val isZoomed = zoomScale > 1.05f

    val coroutineScope = rememberCoroutineScope()
    val currentOffsetX = animOffsetX.value

    // Smooth subtle rotation anchored to horizontal movement (max 15 degrees)
    val rotation = (currentOffsetX / 35f).coerceIn(-15f, 15f)

    val isSwipingRight = currentOffsetX > 40f
    val isSwipingLeft = currentOffsetX < -40f

    val keepCondition = if (swipeConfig == SwipeDirectionConfig.NORMAL) isSwipingRight else isSwipingLeft
    val throwCondition = if (swipeConfig == SwipeDirectionConfig.NORMAL) isSwipingLeft else isSwipingRight

    Card(
        modifier = modifier
            .fillMaxSize()
            .testTag("media_card_${item.assetId}")
            .offset { if (isTopCard) IntOffset(animOffsetX.value.roundToInt(), 0) else IntOffset.Zero }
            .rotate(if (isTopCard) rotation else 0f)
            .shadow(
                elevation = if (isTopCard) (8 + (abs(currentOffsetX) / 30f)).dp.coerceAtMost(20.dp) else 4.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, StoneBorder, RoundedCornerShape(24.dp))
            .background(PureWhite)
            .then(
                if (isTopCard) {
                    Modifier
                        .pointerInput(item.assetId) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (!item.isVideo) {
                                        zoomScale = if (zoomScale > 1f) 1f else 2.5f
                                    }
                                }
                            )
                        }
                        .pointerInput(item.assetId, isZoomed) {
                            if (!isZoomed) {
                                detectDragGestures(
                                    onDragEnd = {
                                        coroutineScope.launch {
                                            if (animOffsetX.value > thresholdPx) {
                                                val decision = if (swipeConfig == SwipeDirectionConfig.NORMAL) DecisionType.KEEP else DecisionType.THROW
                                                launch {
                                                    animOffsetX.animateTo(
                                                        targetValue = 1800f,
                                                        animationSpec = tween(durationMillis = 160, easing = FastOutLinearInEasing)
                                                    )
                                                }
                                                onDecision(decision)
                                            } else if (animOffsetX.value < -thresholdPx) {
                                                val decision = if (swipeConfig == SwipeDirectionConfig.NORMAL) DecisionType.THROW else DecisionType.KEEP
                                                launch {
                                                    animOffsetX.animateTo(
                                                        targetValue = -1800f,
                                                        animationSpec = tween(durationMillis = 160, easing = FastOutLinearInEasing)
                                                    )
                                                }
                                                onDecision(decision)
                                            } else {
                                                animOffsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            animOffsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        coroutineScope.launch {
                                            animOffsetX.snapTo(animOffsetX.value + dragAmount.x)
                                        }
                                    }
                                )
                            }
                        }
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.isVideo && isTopCard) {
                // Native Media3 ExoPlayer Video Player
                VideoPlayerComponent(
                    uriString = item.uriString,
                    isTopCard = isTopCard,
                    settings = settings,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Image or Video Poster Thumbnail
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.uriString)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = zoomScale,
                            scaleY = zoomScale
                        )
                )
            }

            // Star / Favorite indicator
            if (item.isFavorite) {
                Surface(
                    shape = CircleShape,
                    color = Amber500,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
            }

            // Directional overlay during swipe: KEEP
            if (isTopCard && keepCondition) {
                val opacity = ((abs(currentOffsetX) - 30f) / 150f).coerceIn(0f, 0.9f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    KeepEmerald.copy(alpha = opacity * 0.2f),
                                    KeepEmerald.copy(alpha = opacity * 0.65f)
                                )
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = KeepEmerald,
                    shadowElevation = (8 + opacity * 8).dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .rotate(-10f)
                        .scale(1f + opacity * 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KEEP ✨",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            // Directional overlay during swipe: THROW
            if (isTopCard && throwCondition) {
                val opacity = ((abs(currentOffsetX) - 30f) / 150f).coerceIn(0f, 0.9f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ThrowColor.copy(alpha = opacity * 0.65f),
                                    ThrowColor.copy(alpha = opacity * 0.2f)
                                )
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ThrowColor,
                    shadowElevation = (8 + opacity * 8).dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .rotate(10f)
                        .scale(1f + opacity * 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRASH 🗑️",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            // Zoom indicator
            if (isZoomed) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Zoom active (${String.format("%.1f", zoomScale)}x) • Swipes locked",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
