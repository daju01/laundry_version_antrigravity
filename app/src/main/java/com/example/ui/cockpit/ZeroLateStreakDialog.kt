package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.SimulationEngine
import com.example.sim.formatMoney

@Composable
fun ZeroLateStreakDialog(
    engine: SimulationEngine,
    onDismiss: () -> Unit,
    onCelebrate: () -> Unit
) {
    val info = engine.getZeroLateStreakInfo()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dialog")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(22.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF0284C7), Color(0xFF1E293B))),
                width = 2.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("zero_late_streak_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Glowing Shield Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF10B981), Color(0xFF064E3B), Color(0xFF0F172A))
                            )
                        )
                        .border(2.dp, Color(0xFF34D399), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "STREAK BEBAS TELAT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color(0xFF34D399)
                )

                Text(
                    text = "${info.currentStreakDays} HARI BERTURUT-TURUT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = info.tierTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE68A)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Status Hari Ini",
                        value = if (info.todayLateOrders == 0) "0 Terlambat ✅" else "${info.todayLateOrders} Telat ⚠️",
                        color = if (info.todayLateOrders == 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Rekor Tertinggi",
                        value = "${info.highestStreakDays} Hari",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Bonus Kepuasan",
                        value = "+${((info.visualSatisfactionBonusMultiplier - 1.0) * 100).toInt()}% Multiplier",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Akumulasi Bonus",
                        value = formatMoney(info.bonusCashAccumulated),
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Milestone Tier Progress
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🏆 TINGKATAN KEPATUHAN WAKTU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StreakTierRow("1 Hari", "Disiplin Standar (+Rp 40k)", info.currentStreakDays >= 1)
                        StreakTierRow("2 Hari", "Konsisten Bebas Telat (+Rp 80k)", info.currentStreakDays >= 2)
                        StreakTierRow("3 Hari", "Super Disiplin (+Rp 120k)", info.currentStreakDays >= 3)
                        StreakTierRow("5 Hari", "Legendary Flawless (+Rp 200k)", info.currentStreakDays >= 5)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            com.example.audio.GameSoundManager.playStreakBonusChime()
                            onCelebrate()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("celebrate_streak_btn")
                    ) {
                        Text("✨ Rayakan Efisiensi!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun StreakTierRow(label: String, reward: String, isAchieved: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (isAchieved) "✅" else "🔒", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isAchieved) FontWeight.Bold else FontWeight.Normal,
                color = if (isAchieved) Color.White else Color(0xFF64748B)
            )
        }
        Text(
            text = reward,
            fontSize = 10.sp,
            color = if (isAchieved) Color(0xFF34D399) else Color(0xFF64748B),
            fontWeight = FontWeight.Bold
        )
    }
}
