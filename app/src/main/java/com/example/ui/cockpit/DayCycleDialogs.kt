package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.*

@Composable
fun MorningPlanningDialog(
    engine: SimulationEngine,
    onStartDay: (maxQuotaKg: Double, isOvertime: Boolean) -> Unit
) {
    val leftoverKg = engine.ordersList.filter { !it.isCompleted }.sumOf { it.weightKg }
    var quotaSlider by remember { mutableFloatStateOf(engine.maxDailyIntakeKgQuota.toFloat()) }
    var overtimeCheck by remember { mutableStateOf(engine.isOvertimeActiveToday) }

    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
            modifier = Modifier.fillMaxWidth().testTag("morning_planning_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Text(
                    text = "BRIEFING PAGI · HARI ${engine.currentDay}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8), letterSpacing = 1.sp)
                )
                Text(
                    text = "Rencana Operasi Hari Ini",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Condition Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sisa Kemarin", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("${leftoverKg.toInt()} kg", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Prakiraan Cuaca", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("${engine.weatherToday.icon} ${engine.weatherToday.label}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFDE68A))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weather advice warning
                if (engine.weatherToday == Weather.HUJAN && engine.equipmentList.none { it.type == AssetType.DRYER_GAS_10KG }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Peringatan: Hujan deras akan memperlambat jemuran! Batasi kuota masuk.",
                                fontSize = 10.sp,
                                color = Color(0xFFFEE2E2)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Slider Kuota Masuk
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Batas Penerimaan (Kuota):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("${quotaSlider.toInt()} kg/hari", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
                Slider(
                    value = quotaSlider,
                    onValueChange = { quotaSlider = it },
                    valueRange = 10f..40f,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF0284C7), activeTrackColor = Color(0xFF0284C7))
                )

                // Overtime switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rencanakan Lembur (+2 Jam)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Operasi s/d 20:00 (Uang lembur Rp40rb/orang)", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = overtimeCheck,
                        onCheckedChange = { overtimeCheck = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onStartDay(quotaSlider.toDouble(), overtimeCheck) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_start_morning")
                ) {
                    Text("Buka Toko & Jalankan Operasi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
            modifier = Modifier.fillMaxWidth().testTag("evening_summary_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Text(
                    text = "EVALUASI HARI KE-${report.day}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), letterSpacing = 1.sp)
                )
                Text(
                    text = "Laporan Operasi & Keuangan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 Metric Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatBox("Cucian Selesai", "${report.totalKgCompleted.toInt()} kg", Color(0xFF10B981), Modifier.weight(1f))
                    SummaryStatBox("Sisa Backlog", "${report.leftoverBacklogKg.toInt()} kg", if (report.leftoverBacklogKg > 15) Color(0xFFEF4444) else Color(0xFFF59E0B), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatBox("Pendapatan", formatMoney(report.revenueToday), Color.White, Modifier.weight(1f))
                    SummaryStatBox("Laba Bersih", formatMoney(report.netProfitToday), if (report.netProfitToday >= 0) Color(0xFF10B981) else Color(0xFFEF4444), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Financial Breakdown
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biaya Operasional & Gaji", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(formatMoney(report.operatingExpensesToday), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saldo Kas Akhir", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(formatMoney(report.endingCash), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // STREAK STATUS CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (report.isStreakMaintained) Color(0xFF1E293B) else Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (report.isStreakMaintained) Color(0xFFEA580C) else Color(0xFF475569))),
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
                                text = if (report.isStreakMaintained) "STREAK HARI EFISIEN: ${report.efficiencyStreak} HARI!" else "STREAK TERPUTUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (report.isStreakMaintained) Color(0xFFF97316) else Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (report.isStreakMaintained) "Bonus tunai kinerja: +${formatMoney(report.streakBonusCash)}" else "Tingkatkan ketepatan waktu & minimalisasi backlog untuk memulai streak baru.",
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            if (report.zeroLateStreak > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🛡️ Bebas Telat: ${report.zeroLateStreak} Hari Berturut-turut! (Bonus Kepuasan Aktif)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Key Insight & Bottleneck Takeaway
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("💡 Pelajaran Hari Ini:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(report.keyInsight, fontSize = 11.sp, color = Color(0xFFE2E8F0), lineHeight = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onContinueNextDay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_next_day")
                ) {
                    Text("Lanjut Hari Berikutnya (Hari ${report.day + 1})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SummaryStatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
