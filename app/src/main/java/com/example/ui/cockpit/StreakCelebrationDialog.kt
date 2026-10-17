package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.StreakCelebration
import com.example.sim.formatMoney

@Composable
fun StreakCelebrationDialog(
    streak: StreakCelebration,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.2f) }
    LaunchedEffect(streak) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flame_anim")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_pulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFF59E0B), Color(0xFFEF4444))), RoundedCornerShape(24.dp))
                .testTag("streak_celebration_dialog")
        ) {
            // Background Canvas: Fire Embers
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                val flameColors = listOf(Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFEF4444))
                for (i in 0 until 18) {
                    val cx = (size.width * ((i * 19) % 100) / 100f)
                    val cy = (size.height * 0.85f) - ((i * 27) % (size.height * 0.65f).toInt())
                    val radius = 3f + (i % 5)
                    val emberColor = flameColors[i % flameColors.size].copy(alpha = 0.45f)
                    drawCircle(color = emberColor, radius = radius, center = Offset(cx, cy))
                }
            }

            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Giant Pulsing Flame Badge
                Box(
                    modifier = Modifier
                        .scale(flamePulse)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFFFDE68A), Color(0xFFF97316), Color(0xFFDC2626)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = "STREAK HARI EFISIEN!",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF97316),
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "${streak.streakDays} Hari Berturut-turut 🔥",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Operasi cepat, tanpa backlog parah, dan laba positif konsisten!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Streak Multiplier & Reward Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEA580C))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bonus Efisiensi", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEA580C))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("+${streak.multiplierPercent}% Multiplier", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bonus Kas Hari Ini", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("+${formatMoney(streak.bonusCash)} 🪙", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bonus XP Tycoon", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("+${streak.bonusXp} XP ⭐", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_dismiss_streak")
                ) {
                    Text("Pertahankan Rentetan Kemenangan! 🔥", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
