package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DecisionType
import com.example.ui.theme.Amber200
import com.example.ui.theme.Amber500
import com.example.ui.theme.KeepEmerald
import com.example.ui.theme.KeepEmeraldDark
import com.example.ui.theme.ThrowColor
import com.example.ui.theme.ThrowCoralDark
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ActionCelebrationOverlay(
    decision: DecisionType?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (decision == null || decision == DecisionType.NONE || decision == DecisionType.SKIP) return

    val progress = remember(decision) { Animatable(0f) }
    val badgeScale = remember(decision) { Animatable(0.4f) }
    val badgeAlpha = remember(decision) { Animatable(1f) }

    val isKeep = decision == DecisionType.KEEP

    // Generate random particles once per trigger
    val particles = remember(decision) {
        val count = if (isKeep) 22 else 26
        val palette = if (isKeep) {
            listOf(KeepEmerald, KeepEmeraldDark, Amber500, Amber200, Color(0xFF34D399), Color.White)
        } else {
            listOf(ThrowColor, ThrowCoralDark, Color(0xFFB91C1C), Color(0xFFF87171), Color(0xFFEF4444), Color.White)
        }
        val rand = Random(System.currentTimeMillis())
        List(count) {
            val angle = (rand.nextFloat() * 360f) * (Math.PI.toFloat() / 180f)
            val speed = 180f + rand.nextFloat() * 260f
            val size = 6f + rand.nextFloat() * 8f
            Particle(
                angle = angle,
                speed = speed,
                size = size,
                color = palette[rand.nextInt(palette.size)]
            )
        }
    }

    LaunchedEffect(decision) {
        // Run particle expansion and badge pop in parallel
        badgeScale.animateTo(
            targetValue = 1.15f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        badgeScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
        )
    }

    LaunchedEffect(decision) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
        )
        badgeAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 120, easing = LinearEasing)
        )
        onFinished()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Particle Burst Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val p = progress.value
            val alpha = (1f - p).coerceIn(0f, 1f)

            // Expanding ripple wave
            val ringRadius = p * (size.width * 0.45f)
            val ringColor = if (isKeep) KeepEmerald else ThrowColor
            drawCircle(
                color = ringColor.copy(alpha = alpha * 0.35f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = (6f * (1f - p)).coerceAtLeast(1f))
            )

            // Particles radiating outward
            particles.forEach { pt ->
                val distance = pt.speed * p
                val px = center.x + cos(pt.angle) * distance
                val py = center.y + sin(pt.angle) * distance + (p * p * 60f) // gravity pull
                val ptAlpha = (1f - p * 0.9f).coerceIn(0f, 1f)

                drawCircle(
                    color = pt.color.copy(alpha = ptAlpha),
                    radius = (pt.size * (1f - p * 0.4f)).coerceAtLeast(1f),
                    center = Offset(px, py)
                )
            }
        }

        // Animated Central Stamp / Badge
        val currentAlpha = badgeAlpha.value
        if (currentAlpha > 0.05f) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isKeep) KeepEmeraldDark else ThrowCoralDark,
                shadowElevation = 14.dp,
                modifier = Modifier
                    .scale(badgeScale.value)
                    .rotate(if (isKeep) -6f else 8f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = if (isKeep) Icons.Default.CheckCircle else Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isKeep) "KEPT! ✨" else "MARKED TO THROW 🗑️",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
