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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.theme.*

/**
 * Compact, Easy-to-Follow Kid-Friendly Tycoon HUD Header Bar.
 * Designed so the 3D Laundry Diorama below it fits comfortably on screen without scrolling.
 */
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
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(SkyTintBorder, MintTintBorder, SunnyTintBorder)),
            width = 1.5.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1F0EA5E9))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            // Row 1: Level Badge, Store Name & XP Progress + Day & Clock Pill
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(SunnyAmber, CoralRose)))
                            .clickable(enabled = onTestLevelUp != null) { onTestLevelUp?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("level_badge")
                    ) {
                        Text(
                            text = "⭐ Lv.${engine.tycoonLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = engine.growthTier.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900,
                                fontSize = 12.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { xpProgress },
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SunnyAmber,
                                trackColor = SunnyTintBg
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${engine.currentXp}/$nextTierXp XP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        }
                    }
                }

                // Clean Day, Clock & Workday Progress Pill (08:00 -> 18:00/20:00)
                val dayProgress = engine.getWorkdayProgressFraction()
                val closeLabel = if (engine.isOvertimeActiveToday) "20:00" else "18:00"
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (engine.isLabMode) LavenderTintBg else SkyTintBg)
                        .border(
                            1.dp,
                            if (engine.isLabMode) BerryLavender else SkyTintBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (engine.isLabMode) {
                                "🧪 LAB • ${engine.getFormattedTime()}"
                            } else {
                                "Hari ${engine.currentDay} • ${engine.getFormattedTime()}/$closeLabel"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (engine.isLabMode) BerryLavenderDark else OceanBlueDeep
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = engine.weatherToday.icon, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { dayProgress },
                        modifier = Modifier
                            .width(86.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (dayProgress > 0.8f) CoralRose else OceanBluePrimary,
                        trackColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: 4 Kid-Friendly Metric Boxes (Uang Kas, Selesai, Antrean, Rating)
            val backlogKg = engine.ordersList.filter { !it.isCompleted }.sumOf { it.weightKg }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorfulHudMetricPill(
                    icon = "🪙",
                    label = "Uang Kas",
                    value = formatMoneyCompact(engine.cash),
                    bgColor = SunnyTintBg,
                    borderColor = SunnyTintBorder,
                    valueColor = SunnyAmberDark,
                    modifier = Modifier.weight(1.1f)
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
                    icon = "🧺",
                    label = "Antrean",
                    value = "${backlogKg.toInt()} kg",
                    bgColor = if (backlogKg > 20) CoralTintBg else SkyTintBg,
                    borderColor = if (backlogKg > 20) CoralTintBorder else SkyTintBorder,
                    valueColor = if (backlogKg > 20) CoralRoseDark else OceanBlueDeep,
                    modifier = Modifier.weight(0.95f)
                )
                ColorfulHudMetricPill(
                    icon = "⭐",
                    label = "Bintang",
                    value = "${String.format("%.1f", engine.reputationStars)}★",
                    bgColor = LavenderTintBg,
                    borderColor = LavenderTintBorder,
                    valueColor = BerryLavenderDark,
                    modifier = Modifier.weight(0.95f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Speed Controls & Quick Boosters in one clean row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Slate100)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SpeedChip("⏸", engine.simSpeed == SimSpeed.PAUSED) { onSpeedChanged(SimSpeed.PAUSED) }
                    SpeedChip("1x", engine.simSpeed == SimSpeed.SPEED_1X) { onSpeedChanged(SimSpeed.SPEED_1X) }
                    SpeedChip("2x", engine.simSpeed == SimSpeed.SPEED_2X) { onSpeedChanged(SimSpeed.SPEED_2X) }
                    SpeedChip("5x", engine.simSpeed == SimSpeed.SPEED_5X) { onSpeedChanged(SimSpeed.SPEED_5X) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GameBoosterChip("☕ Kopi", SunnyTintBg, SunnyTintBorder, SunnyAmberDark) { onCoffeeBoost() }
                    GameBoosterChip("⚡ Turbo", SkyTintBg, SkyTintBorder, OceanBlueDeep) { onTurboWash() }
                    GameBoosterChip("📢 Promo", MintTintBg, MintTintBorder, MintEmeraldDark) { onPromoFlyer() }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Row 4: Clean Management Action Bar (Toko, Lab, Tutup Hari)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudActionPill(
                    label = "🏪 Beli Alat & Staf",
                    bgColor = OceanBluePrimary,
                    tag = "btn_open_shop",
                    modifier = Modifier.weight(1.1f),
                    onClick = onOpenShop
                )
                HudActionPill(
                    label = if (engine.isLabMode) "🧪 Keluar Lab" else "🧪 Coba di Lab",
                    bgColor = BerryLavender,
                    tag = "btn_open_lab",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenLab
                )
                HudActionPill(
                    label = "🌙 Selesai Hari",
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
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Text(
            text = "$icon $label",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = Slate600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
            .padding(vertical = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
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
            .clip(RoundedCornerShape(7.dp))
            .background(if (isSelected) OceanBluePrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else Slate700
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
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            maxLines = 1
        )
    }
}
