package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun EquipmentShopModal(
    engine: SimulationEngine,
    onBuyEquipment: (AssetType) -> Boolean,
    onHireStaff: (name: String, phase: StationPhase) -> Boolean,
    onDismiss: () -> Unit
) {
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp).testTag("shop_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOKO INVESTASI & TIM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                        )
                        Text(
                            text = "Kas Tersedia: ${formatMoney(engine.cash)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                    }
                }

                if (feedbackMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(feedbackMessage!!, fontSize = 11.sp, color = Color(0xFFFDE68A), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("ALAT & MESIN PENGEMBANGAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    }

                    val shopAssets = listOf(
                        Triple(AssetType.DRYER_GAS_10KG, "Bebas dari kendala hujan! Kering stabil 45 mnt.", "Sangat Direkomendasikan"),
                        Triple(AssetType.SETRIKA_UAP_BOILER, "Setrika 1.6x lebih cepat, kurangi lelah staf.", "Atasi Bottleneck Setrika"),
                        Triple(AssetType.MESIN_CUCI_7KG, "Tambah kapasitas cuci paralel +7 kg.", "Ekspansi Kapasitas"),
                        Triple(AssetType.MESIN_CUCI_14KG, "Mesin cuci jumbo muat bedcover/karpet.", "Kapasitas Komersial")
                    )

                    items(shopAssets) { (asset, benefit, tag) ->
                        val canAfford = engine.cash >= asset.purchaseCost
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(asset.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(tag, fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(formatMoney(asset.purchaseCost), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (canAfford) Color(0xFF10B981) else Color(0xFFEF4444))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(benefit, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        val success = onBuyEquipment(asset)
                                        feedbackMessage = if (success) "Berhasil membeli ${asset.displayName}!" else "Kas tidak cukup!"
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(34.dp)
                                ) {
                                    Text(if (canAfford) "Beli & Pasang Sekarang" else "Kas Tidak Cukup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("REKRUTMEN PEKERJA BARU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    }

                    item {
                        val hireCost = 500_000.0
                        val canHire = engine.cash >= hireCost
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Rekrut Operator Tambahan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Gaji Rp90.000/hari", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    Text(formatMoney(hireCost), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        val names = listOf("Rudi", "Nurul", "Agus", "Wati")
                                        val nextName = names[engine.workerList.size % names.size]
                                        val success = onHireStaff(nextName, StationPhase.SETRIKA)
                                        feedbackMessage = if (success) "Berhasil merekrut $nextName!" else "Kas tidak cukup!"
                                    },
                                    enabled = canHire,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(34.dp)
                                ) {
                                    Text("Rekrut Staf (Seragam & Onboarding)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabExperimentModal(
    engine: SimulationEngine,
    onApplyDecisionToActive: (dryerBought: Boolean, staffHired: Boolean, extraOvertime: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var testBuyDryer by remember { mutableStateOf(false) }
    var testHireStaff by remember { mutableStateOf(false) }
    var testOvertime by remember { mutableStateOf(false) }

    // Project 7 days comparison
    val baselineDailyRevenue = engine.maxDailyIntakeKgQuota * 9_000.0
    val baseline7DayRev = baselineDailyRevenue * 7.0
    val baseline7DayCost = (engine.workerList.size * 90_000.0 + 35_000.0) * 7.0
    val baseline7DayProfit = baseline7DayRev - baseline7DayCost

    val expKgMultiplier = (if (testBuyDryer) 1.35 else 1.0) * (if (testHireStaff) 1.25 else 1.0)
    val exp7DayRev = (baselineDailyRevenue * expKgMultiplier) * 7.0
    val investCost = (if (testBuyDryer) 9_500_000.0 else 0.0) + (if (testHireStaff) 500_000.0 else 0.0)
    val extraWages = (if (testHireStaff) 90_000.0 else 0.0) * 7.0
    val extraOvertimeCost = (if (testOvertime) 40_000.0 * engine.workerList.size else 0.0) * 7.0
    val exp7DayCost = baseline7DayCost + extraWages + extraOvertimeCost
    val exp7DayProfit = exp7DayRev - exp7DayCost

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD97706))),
            modifier = Modifier.fillMaxWidth().testTag("lab_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧪", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("LAB EKSPERIMEN", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFDE68A))
                            Text("Uji Keputusan Tanpa Risiko", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("PILIH SKENARIO PENGUJIAN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(6.dp))

                // Toggle 1: Buy Dryer
                LabToggleRow("Beli Dryer Gas (Rp9.5jt)", "Hilangkan resiko hujan & percepat alur", testBuyDryer) { testBuyDryer = it }
                // Toggle 2: Hire staff
                LabToggleRow("Rekrut 1 Staf Setrika", "Atasi antrean setrika uap", testHireStaff) { testHireStaff = it }
                // Toggle 3: Overtime
                LabToggleRow("Jalankan Lembur 7 Hari", "Operasi ekstra untuk kejar pesanan", testOvertime) { testOvertime = it }

                Spacer(modifier = Modifier.height(12.dp))

                // Comparison Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("PROYEKSI 7 HARI KE DEPAN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Baseline (Kondisi Sekarang)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(formatMoney(baseline7DayProfit), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Setelah Keputusan Diuji", fontSize = 11.sp, color = Color(0xFFFDE68A))
                            Text(formatMoney(exp7DayProfit), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (exp7DayProfit > baseline7DayProfit) Color(0xFF10B981) else Color(0xFFEF4444))
                        }
                        if (investCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pengeluaran Kas Awal: ${formatMoney(investCost)}", fontSize = 10.sp, color = Color(0xFFFCA5A5))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("Batal", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = { onApplyDecisionToActive(testBuyDryer, testHireStaff, testOvertime) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).height(40.dp).testTag("btn_apply_lab")
                    ) {
                        Text("Terapkan ke Usaha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LabToggleRow(label: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 9.sp, color = Color(0xFF94A3B8))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD97706))
        )
    }
}

@Composable
fun OnboardingTutorialDialog(
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF38BDF8))),
            modifier = Modifier.fillMaxWidth().testTag("onboarding_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PANDUAN MULAI CEPAT (${step}/3)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8), letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (step) {
                    1 -> {
                        Text("1. Jalankan Waktu & Amati Aliran", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Usaha Anda dimulai dari skala rumahan (0–20 kg) dengan 1 mesin cuci, jemuran luar ruangan, meja setrika, 2 pekerja (Budi & Siti), dan kas modal Rp5.000.000. Gunakan tombol kecepatan [1x] [2x] [5x] untuk menjalankan waktu operasional.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), lineHeight = 17.sp)
                        )
                    }
                    2 -> {
                        Text("2. Ketuk Stasiun untuk Deteksi Hambatan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ketuk stasiun 3D (Cuci, Jemur, Setrika, Packing) untuk melihat antrean kg. Saat antrean memburuk, stasiun akan menyala merah. Ketukan membuka penjelasan konkret akar masalah dan solusi cepat.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), lineHeight = 17.sp)
                        )
                    }
                    3 -> {
                        Text("3. Uji Keputusan & Kembangkan Usaha", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Gunakan tombol [Lab] untuk menguji pembelian Dryer gas, merekrut staf, atau lembur tanpa risiko. Tutup hari setiap sore untuk melihat laba bersih dan evaluasi sebab-akibat.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), lineHeight = 17.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (step > 1) {
                        TextButton(onClick = { step-- }) {
                            Text("Kembali", color = Color(0xFF94A3B8))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 3) step++ else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (step < 3) "Lanjut" else "Mulai Operasi!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
