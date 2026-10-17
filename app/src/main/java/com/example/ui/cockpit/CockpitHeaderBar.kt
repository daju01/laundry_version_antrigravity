package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*

@Composable
fun CockpitHeaderBar(
    engine: SimulationEngine,
    onSpeedChanged: (SimSpeed) -> Unit,
    onOpenLab: () -> Unit,
    onOpenShop: () -> Unit,
    onManualCloseDay: () -> Unit,
    onCoffeeBoost: () -> Unit,
    onTurboWash: () -> Unit,
    onPromoFlyer: () -> Unit,
    onTestLevelUp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Row 1: Tycoon Level Bar & XP Progress
            val nextTierXp = when (engine.tycoonLevel) {
                1 -> 150
                2 -> 450
                3 -> 1000
                else -> 2000
            }
            val xpProgress = (engine.currentXp.toFloat() / nextTierXp.toFloat()).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B))))
                            .clickable(enabled = onTestLevelUp != null) { onTestLevelUp?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("level_badge")
                    ) {
                        Text(
                            text = "⭐ Lv.${engine.tycoonLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 11.sp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = engine.growthTier.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 11.sp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { xpProgress },
                                modifier = Modifier.width(70.dp).height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFFBBF24),
                                trackColor = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${engine.currentXp}/${nextTierXp} XP", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                // Clock & Weather & Mode
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (engine.isLabMode) Color(0xFFD97706) else Color(0xFF0F172A))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (engine.isLabMode) "🧪 LAB" else "Hari ${engine.currentDay} · ${engine.getFormattedTime()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (engine.isLabMode) Color.White else Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = engine.weatherToday.icon, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Tycoon Counters (Kas 🪙, Selesai 🧺, Antrean ⏳, Rating ⭐)
            val backlogKg = engine.ordersList.filter { !it.isCompleted }.sumOf { it.weightKg }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kas 🪙
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatMoneyCompact(engine.cash),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFBBF24)
                    )
                }

                // Selesai 🧺
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${engine.completedKgToday.toInt()}kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                // Antrean ⏳
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏳", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${backlogKg.toInt()}kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (backlogKg > 20) Color(0xFFEF4444) else Color(0xFFF59E0B)
                    )
                }

                // STREAK FLAME BADGE 🔥
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (engine.currentStreak > 1) Color(0xFFEA580C) else Color(0xFF334155))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${engine.currentStreak}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (engine.currentStreak > 1) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                // ZERO LATE SHIELD BADGE 🛡️
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (engine.zeroLateStreakDays > 0) Color(0xFF065F46) else Color(0xFF334155))
                        .border(1.dp, if (engine.zeroLateStreakDays > 0) Color(0xFF10B981) else Color.Transparent, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${engine.zeroLateStreakDays}d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (engine.zeroLateStreakDays > 0) Color(0xFF34D399) else Color(0xFF94A3B8)
                        )
                    }
                }

                // Reputasi ⭐
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", engine.reputationStars),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            // Real-time Task Feedback & Combo Strip (if recent feedback exists)
            val feedback = engine.latestCompletionFeedback
            val isFreshFeedback = feedback != null && (System.currentTimeMillis() - feedback.timestamp < 3500)
            if (isFreshFeedback) {
                val activeFeedback = feedback!!
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF065F46), Color(0xFF0E7490))))
                        .border(1.dp, Color(0xFF34D399), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${feedback.title}: ${feedback.subtitle}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${feedback.xpEarned} XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFDE68A)
                            )
                            if (feedback.comboCount > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEA580C))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Combo x${feedback.comboCount}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Speed Controls & Quick Boost Buttons!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speeds
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    SpeedChip("⏸", engine.simSpeed == SimSpeed.PAUSED) { onSpeedChanged(SimSpeed.PAUSED) }
                    SpeedChip("1x", engine.simSpeed == SimSpeed.SPEED_1X) { onSpeedChanged(SimSpeed.SPEED_1X) }
                    SpeedChip("2x", engine.simSpeed == SimSpeed.SPEED_2X) { onSpeedChanged(SimSpeed.SPEED_2X) }
                    SpeedChip("5x", engine.simSpeed == SimSpeed.SPEED_5X) { onSpeedChanged(SimSpeed.SPEED_5X) }
                }

                // Action Boosters
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GameBoosterChip("☕ Kopi", "Pulihkan Staf") { onCoffeeBoost() }
                    GameBoosterChip("⚡ Turbo", "Percepat Cuci") { onTurboWash() }
                    GameBoosterChip("📢 Promo", "+2 Order") { onPromoFlyer() }
                }
            }
        }
    }
}

@Composable
private fun SpeedChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A))
            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun GameBoosterChip(label: String, tooltip: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF334155))
            .border(1.dp, Color(0xFF64748B), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFDE68A)
        )
    }
}
