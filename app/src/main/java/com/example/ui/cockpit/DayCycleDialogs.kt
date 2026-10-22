package com.example.ui.cockpit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.*
import com.example.ui.theme.*

@Composable
fun MorningPlanningDialog(
    engine: SimulationEngine,
    onStartDay: (maxQuotaKg: Double, isOvertime: Boolean) -> Unit
) {
    val leftoverKg = engine.ordersList.filter { !it.isCompleted }.sumOf { it.weightKg }
    val maxSliderRange = maxOf(40f, engine.growthTier.maxDailyCapacityTargetKg.toFloat() * 1.25f)
    var quotaSlider by remember(engine.maxDailyIntakeKgQuota, maxSliderRange) {
        mutableFloatStateOf(engine.maxDailyIntakeKgQuota.toFloat().coerceIn(10f, maxSliderRange))
    }
    var overtimeCheck by remember { mutableStateOf(engine.isOvertimeActiveToday) }

    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkyTintBorder), width = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("morning_planning_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "☀️ BRIEFING PAGI • HARI ${engine.currentDay}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary, letterSpacing = 1.sp)
                )
                Text(
                    text = "Rencana Operasi Hari Ini",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SkyTintBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sisa Kemarin", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OceanBlueDeep)
                            Text("${leftoverKg.toInt()} kg", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SunnyTintBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Prakiraan Cuaca", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunnyAmberDark)
                            Text("${engine.weatherToday.icon} ${engine.weatherToday.label}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (engine.weatherToday == Weather.HUJAN && engine.equipmentList.none { it.type == AssetType.DRYER_GAS_10KG }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CoralTintBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CoralRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hujan deras memperlambat jemuran! Sesuaikan kuota harian.",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = CoralRoseDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Batas Penerimaan (Kuota):", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
                    Text("${quotaSlider.toInt()} kg/hari", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary)
                }
                Slider(
                    value = quotaSlider.coerceIn(10f, maxSliderRange),
                    onValueChange = { quotaSlider = it },
                    valueRange = 10f..maxSliderRange,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = OceanBluePrimary, activeTrackColor = OceanBluePrimary, inactiveTrackColor = SkyTintBg)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rencanakan Lembur (+2 Jam)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Text("Operasi s/d 20:00 (Upah lembur Rp40rb/orang)", fontSize = 10.sp, color = Slate500)
                    }
                    Switch(
                        checked = overtimeCheck,
                        onCheckedChange = { overtimeCheck = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OceanBluePrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onStartDay(quotaSlider.toDouble(), overtimeCheck) },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_start_morning")
                ) {
                    Text("Buka Toko & Jalankan Operasi", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun EveningSummaryDialog(
    report: DailySummaryReport,
    onContinueNextDay: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MintTintBorder), width = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("evening_summary_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🌙 EVALUASI HARI KE-${report.day}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = MintEmeraldDark, letterSpacing = 1.sp)
                )
                Text(
                    text = "Laporan Operasi & Keuangan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatBox("Cucian Selesai", "${report.totalKgCompleted.toInt()} kg", MintTintBg, MintEmeraldDark, Modifier.weight(1f))
                    SummaryStatBox(
                        "Sisa Backlog",
                        "${report.leftoverBacklogKg.toInt()} kg",
                        if (report.leftoverBacklogKg > 15) CoralTintBg else SunnyTintBg,
                        if (report.leftoverBacklogKg > 15) CoralRoseDark else SunnyAmberDark,
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatBox("Pendapatan", formatMoney(report.revenueToday), SkyTintBg, OceanBlueDeep, Modifier.weight(1f))
                    SummaryStatBox(
                        "Laba Bersih",
                        formatMoney(report.netProfitToday),
                        if (report.netProfitToday >= 0) MintTintBg else CoralTintBg,
                        if (report.netProfitToday >= 0) MintEmeraldDark else CoralRoseDark,
                        Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biaya Operasional & Gaji", fontSize = 11.sp, color = Slate600)
                            Text(formatMoney(report.operatingExpensesToday), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralRoseDark)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saldo Kas Akhir", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            Text(formatMoney(report.endingCash), fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = OceanBlueDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = if (report.isStreakMaintained) SunnyTintBg else Slate100),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (report.isStreakMaintained) "🔥" else "⚠️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (report.isStreakMaintained) "STREAK HARI EFISIEN: ${report.efficiencyStreak} HARI!" else "STREAK BELUM TERCAPAI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (report.isStreakMaintained) SunnyAmberDark else Slate700
                            )
                            Text(
                                text = if (report.isStreakMaintained) "Bonus tunai kinerja: +${formatMoney(report.streakBonusCash)}" else "Minimalisasi backlog & jaga laba positif besok.",
                                fontSize = 10.5.sp,
                                color = Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SkyTintBg.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("💡 Catatan Evaluasi:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = OceanBlueDeep)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(report.keyInsight, fontSize = 11.sp, color = Slate800, lineHeight = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onContinueNextDay,
                    colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_next_day")
                ) {
                    Text("Lanjut ke Hari ${report.day + 1} ➔", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SummaryStatBox(
    label: String,
    value: String,
    bgColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate600)
            Text(value, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        }
    }
}
