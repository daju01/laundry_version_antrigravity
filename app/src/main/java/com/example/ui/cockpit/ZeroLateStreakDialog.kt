package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.*

@Composable
fun ZeroLateStreakDialog(
    engine: SimulationEngine,
    onDismiss: () -> Unit,
    onCelebrate: () -> Unit
) {
    val info = engine.getZeroLateStreakInfo()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(MintEmerald, OceanBluePrimary)),
                width = 2.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("zero_late_streak_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(MintEmerald, TealAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 30.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "STREAK BEBAS TELAT",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MintEmeraldDark
                )

                Text(
                    text = "${info.currentStreakDays} HARI BERTURUT-TURUT",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = info.tierTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlueDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Status Hari Ini",
                        value = if (info.todayLateOrders == 0) "0 Terlambat ✅" else "${info.todayLateOrders} Telat ⚠️",
                        bgColor = if (info.todayLateOrders == 0) MintTintBg else CoralTintBg,
                        color = if (info.todayLateOrders == 0) MintEmeraldDark else CoralRoseDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Rekor Tertinggi",
                        value = "${info.highestStreakDays} Hari",
                        bgColor = SkyTintBg,
                        color = OceanBlueDeep,
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
                        bgColor = SunnyTintBg,
                        color = SunnyAmberDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Akumulasi Bonus",
                        value = formatMoney(info.bonusCashAccumulated),
                        bgColor = LavenderTintBg,
                        color = BerryLavenderDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🏆 TINGKATAN KEPATUHAN WAKTU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StreakTierRow("1 Hari", "Disiplin Standar (+Rp 40k)", info.currentStreakDays >= 1)
                        StreakTierRow("2 Hari", "Konsisten Bebas Telat (+Rp 80k)", info.currentStreakDays >= 2)
                        StreakTierRow("3 Hari", "Super Disiplin (+Rp 120k)", info.currentStreakDays >= 3)
                        StreakTierRow("5 Hari", "Legendary Flawless (+Rp 200k)", info.currentStreakDays >= 5)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            com.example.audio.GameSoundManager.playStreakBonusChime()
                            onCelebrate()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("celebrate_streak_btn")
                    ) {
                        Text("✨ Klaim Bonus!", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
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
    bgColor: Color,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Slate600)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = color)
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
                color = if (isAchieved) Slate900 else Slate400
            )
        }
        Text(
            text = reward,
            fontSize = 10.5.sp,
            color = if (isAchieved) MintEmeraldDark else Slate400,
            fontWeight = FontWeight.Bold
        )
    }
}
