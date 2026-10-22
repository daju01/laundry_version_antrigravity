package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.theme.*

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(SkyTintBorder, MintTintBorder, SunnyTintBorder)),
            width = 1.5.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x1F0EA5E9))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Level Badge, Store Tier Title, XP Bar, & Day/Clock Pill
            val nextTierXp = GrowthTier.values()
                .firstOrNull { it.levelNumber == engine.tycoonLevel + 1 }
                ?.requiredXp
                ?: GrowthTier.INDUSTRI.requiredXp
            val xpProgress = (engine.currentXp.toFloat() / nextTierXp.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(SunnyAmber, CoralRose)))
                            .clickable(enabled = onTestLevelUp != null) { onTestLevelUp?.invoke() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("level_badge")
                    ) {
                        Text(
                            text = "⭐ Lv.${engine.tycoonLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = engine.growthTier.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { xpProgress },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SunnyAmber,
                                trackColor = SunnyTintBg
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${engine.currentXp}/$nextTierXp XP",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        }
                    }
                }

                // Clean Day, Clock & Weather Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (engine.isLabMode) LavenderTintBg else SkyTintBg)
                        .border(
                            1.dp,
                            if (engine.isLabMode) BerryLavender else SkyTintBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (engine.isLabMode) "🧪 MODE LAB" else "Hari ${engine.currentDay} • ${engine.getFormattedTime()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (engine.isLabMode) BerryLavenderDark else OceanBlueDeep
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = engine.weatherToday.icon, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: 4 Clean Colorful Metric Pills (Kas, Selesai, Antrean, Reputasi/Streak)
            val backlogKg = engine.ordersList.filter { !it.isCompleted }.sumOf { it.weightKg }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorfulHudMetricPill(
                    icon = "🪙",
                    label = "Saldo Kas",
                    value = formatMoneyCompact(engine.cash),
                    bgColor = SunnyTintBg,
                    borderColor = SunnyTintBorder,
                    valueColor = SunnyAmberDark,
                    modifier = Modifier.weight(1.15f)
                )
                ColorfulHudMetricPill(
                    icon = "✨",
                    label = "Selesai",
                    value = "${engine.completedKgToday.toInt()} kg",
                    bgColor = MintTintBg,
                    borderColor = MintTintBorder,
                    valueColor = MintEmeraldDark,
                    modifier = Modifier.weight(0.95f)
                )
                ColorfulHudMetricPill(
                    icon = "⏳",
                    label = "Antrean",
                    value = "${backlogKg.toInt()} kg",
                    bgColor = if (backlogKg > 20) CoralTintBg else SkyTintBg,
                    borderColor = if (backlogKg > 20) CoralTintBorder else SkyTintBorder,
                    valueColor = if (backlogKg > 20) CoralRoseDark else OceanBlueDeep,
                    modifier = Modifier.weight(0.95f)
                )
                ColorfulHudMetricPill(
                    icon = "⭐",
                    label = "Rating • 🔥${engine.currentStreak}x",
                    value = "${String.format("%.1f", engine.reputationStars)}★",
                    bgColor = LavenderTintBg,
                    borderColor = LavenderTintBorder,
                    valueColor = BerryLavenderDark,
                    modifier = Modifier.weight(1.05f)
                )
            }

            // Real-time Task Completion Feedback Banner
            val feedback = engine.latestCompletionFeedback
            val isFreshFeedback = feedback != null && (System.currentTimeMillis() - feedback.timestamp < 3500)
            if (isFreshFeedback) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(MintEmerald, OceanBluePrimary)))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ ${feedback.title}: ${feedback.subtitle}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "+${feedback.xpEarned} XP ${if (feedback.comboCount > 1) "🔥x${feedback.comboCount}" else ""}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFEF08A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Speed Controls & Colorful Booster Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented Speed Control
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SpeedChip("⏸", engine.simSpeed == SimSpeed.PAUSED) { onSpeedChanged(SimSpeed.PAUSED) }
                    SpeedChip("1x", engine.simSpeed == SimSpeed.SPEED_1X) { onSpeedChanged(SimSpeed.SPEED_1X) }
                    SpeedChip("2x", engine.simSpeed == SimSpeed.SPEED_2X) { onSpeedChanged(SimSpeed.SPEED_2X) }
                    SpeedChip("5x", engine.simSpeed == SimSpeed.SPEED_5X) { onSpeedChanged(SimSpeed.SPEED_5X) }
                }

                // Colorful Boosters
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    GameBoosterChip("☕ Kopi", SunnyTintBg, SunnyTintBorder, SunnyAmberDark) { onCoffeeBoost() }
                    GameBoosterChip("⚡ Turbo", SkyTintBg, SkyTintBorder, OceanBlueDeep) { onTurboWash() }
                    GameBoosterChip("📢 Promo", MintTintBg, MintTintBorder, MintEmeraldDark) { onPromoFlyer() }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 4: Clean Management Action Bar (Toko, Lab, Tutup Hari)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudActionPill(
                    label = "🏪 Toko & Staf",
                    bgColor = OceanBluePrimary,
                    tag = "btn_open_shop",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenShop
                )
                HudActionPill(
                    label = if (engine.isLabMode) "🧪 Keluar Lab" else "🧪 Uji Lab",
                    bgColor = BerryLavender,
                    tag = "btn_open_lab",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenLab
                )
                HudActionPill(
                    label = "🌙 Tutup Hari",
                    bgColor = MintEmerald,
                    tag = "btn_close_day",
                    modifier = Modifier.weight(1f),
                    onClick = onManualCloseDay
                )
            }
        }
    }
}

@Composable
private fun ColorfulHudMetricPill(
    icon: String,
    label: String,
    value: String,
    bgColor: Color,
    borderColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$icon $label",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Slate600,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun HudActionPill(
    label: String,
    bgColor: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun SpeedChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) OceanBluePrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) Color.White else Slate600
        )
    }
}

@Composable
private fun GameBoosterChip(
    label: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}
