package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.KeepContainerLight
import com.example.ui.theme.KeepEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.ThrowColor
import com.example.ui.theme.ThrowContainerLight

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("onboarding_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = PureWhite,
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                // Step Badge
                Surface(
                    shape = CircleShape,
                    color = Amber100,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (step) {
                            1 -> Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Amber600, modifier = Modifier.size(32.dp))
                            2 -> Icon(imageVector = Icons.Default.Swipe, contentDescription = null, tint = Amber600, modifier = Modifier.size(32.dp))
                            else -> Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = KeepEmerald, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                when (step) {
                    1 -> {
                        Text(
                            text = "Make room for what matters.",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Clean up your camera roll and videos without stress. Swipe Clean makes sorting fast, private, and 100% stored in local storage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    2 -> {
                        Text(
                            text = "Swipe to decide.",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(shape = CircleShape, color = KeepContainerLight, modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = KeepEmerald)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Swipe Right", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkPrimary)
                                Text("Keep original", fontSize = 11.sp, color = TextDarkSecondary)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(shape = CircleShape, color = ThrowContainerLight, modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ThrowColor)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Swipe Left", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkPrimary)
                                Text("Review for removal", fontSize = 11.sp, color = TextDarkSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Visible buttons and multi-step Undo are always available if you change your mind.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = "Zero silent deletions.",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nothing is deleted until you review and confirm in the Review tab. The system will always ask for official approval before moving items to Gallery Trash.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (step == i + 1) 18.dp else 8.dp, 8.dp)
                                .background(
                                    if (step == i + 1) Amber600 else Amber100,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (step < 3) {
                Button(
                    onClick = { step++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next", color = Color.White)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("get_started_button")
                ) {
                    Text("Get Started", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip", color = TextDarkSecondary)
            }
        }
    )
}
