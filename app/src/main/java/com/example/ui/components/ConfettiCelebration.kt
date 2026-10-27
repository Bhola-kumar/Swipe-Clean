package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.KeepEmerald
import com.example.ui.theme.ThrowColor
import kotlin.random.Random

private data class ConfettiPiece(
    val initialX: Float,
    val speedY: Float,
    val driftX: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
fun ConfettiCelebration(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val pieces = remember {
        val colors = listOf(
            Amber500,
            Amber600,
            KeepEmerald,
            ThrowColor,
            Color(0xFF38BDF8),
            Color(0xFFA855F7),
            Color(0xFFFBBF24)
        )
        val rand = Random(42)
        List(40) {
            ConfettiPiece(
                initialX = rand.nextFloat(),
                speedY = 0.5f + rand.nextFloat() * 0.7f,
                driftX = (rand.nextFloat() - 0.5f) * 0.2f,
                size = 8f + rand.nextFloat() * 10f,
                color = colors[rand.nextInt(colors.size)],
                isCircle = rand.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = progress.value
        val w = size.width
        val h = size.height

        pieces.forEach { piece ->
            val y = ((p * piece.speedY * h) % h)
            val x = (piece.initialX * w + (p * piece.driftX * w)) % w

            if (piece.isCircle) {
                drawCircle(
                    color = piece.color.copy(alpha = 0.85f),
                    radius = piece.size / 2f,
                    center = Offset(x, y)
                )
            } else {
                drawRect(
                    color = piece.color.copy(alpha = 0.85f),
                    topLeft = Offset(x, y),
                    size = Size(piece.size, piece.size * 0.6f)
                )
            }
        }
    }
}
