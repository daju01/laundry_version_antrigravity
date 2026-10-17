package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.SimulationEngine
import com.example.sim.formatMoney
import com.example.sim.formatMoneyCompact

/**
 * Indikator Streak Efisiensi di UI Utama yang melacak jumlah hari berturut-turut
 * tanpa pesanan terlambat (Zero Late Orders) dan memberikan bonus kepuasan visual.
 */
@Composable
fun ZeroLateStreakCard(
    engine: SimulationEngine,
    onOpenDetails: () -> Unit,
    onClaimSatisfactionBonus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = engine.getZeroLateStreakInfo()

    // Shimmer / Pulse animation for visual satisfaction
    val infiniteTransition = rememberInfiniteTransition(label = "streak_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColors = if (info.todayLateOrders == 0) {
        listOf(
            Color(0xFF10B981).copy(alpha = glowAlpha),
            Color(0xFF06B6D4),
            Color(0xFFF59E0B).copy(alpha = glowAlpha)
        )
    } else {
        listOf(Color(0xFFEF4444), Color(0xFFF97316))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(borderColors),
            width = 1.5.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = if (info.todayLateOrders == 0) Color(0xFF10B981) else Color(0xFFEF4444))
            .clickable { onOpenDetails() }
            .testTag("zero_late_streak_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Badge & Current Streak Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (info.todayLateOrders == 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                        Color(0xFF1E293B)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (info.todayLateOrders == 0) "🛡️" else "⚠️",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STREAK BEBAS TELAT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (info.todayLateOrders == 0) Color(0xFF34D399) else Color(0xFFFCA5A5)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Pulsing flame pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF059669), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "🔥 ${info.currentStreakDays} HARI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                        Text(
                            text = info.tierTitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Visual Satisfaction Multiplier Badge
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF047857), Color(0xFF0284C7))
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+${((info.visualSatisfactionBonusMultiplier - 1.0) * 100).toInt()}% KEPUASAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⭐ Reputasi ${(engine.reputationStars * 10).toInt() / 10.0}/5.0",
                        fontSize = 9.sp,
                        color = Color(0xFFFCD34D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Card: Real-Time Zero Late Monitor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today's zero-late status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = if (info.todayLateOrders == 0) Color(0xFF10B981) else Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (info.todayLateOrders == 0) "Hari Ini: 0 Pesanan Terlambat (100% On-Time)" else "Hari Ini: ${info.todayLateOrders} Terlambat!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (info.todayLateOrders == 0) Color(0xFFE2E8F0) else Color(0xFFFCA5A5)
                        )
                        Text(
                            text = if (info.todayLateOrders == 0) "Streak aktif! Pertahankan ritme hingga tutup kasir malam." else "Gunakan Kopi/Turbo Wash untuk mencegah keterlambatan lagi!",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Daily On-Time percentage
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (info.todayLateOrders == 0) Color(0x3310B981) else Color(0x33EF4444))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${info.todayOnTimePercentage.toInt()}% ON-TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (info.todayLateOrders == 0) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Visual Satisfaction Bonus Claim Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bonus Tunai: +${formatMoneyCompact(info.bonusCashAccumulated)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFBBF24)
                    )
                }

                // Interactive satisfaction button with particles and haptic delight
                Button(
                    onClick = {
                        com.example.audio.GameSoundManager.playStreakBonusChime()
                        onClaimSatisfactionBonus()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!engine.satisfactionBonusClaimedToday) Color(0xFF059669) else Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("claim_satisfaction_bonus_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (!engine.satisfactionBonusClaimedToday) "✨ Klaim Bonus Kepuasan" else "✅ Terklaim Hari Ini",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
