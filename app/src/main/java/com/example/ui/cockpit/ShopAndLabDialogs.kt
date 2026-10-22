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
import com.example.ui.theme.*

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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkyTintBorder), width = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 540.dp)
                .testTag("shop_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🏪 TOKO INVESTASI & REKRUTMEN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary)
                        )
                        Text(
                            text = "Kas: ${formatMoney(engine.cash)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                if (feedbackMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MintTintBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(feedbackMessage!!, fontSize = 11.sp, color = MintEmeraldDark, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("MESIN & PERALATAN PRODUKSI", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate500)
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
                            colors = CardDefaults.cardColors(containerColor = Slate50),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${asset.icon} ${asset.displayName}", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                                        Text(tag, fontSize = 10.sp, color = OceanBlueDark, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = formatMoney(asset.purchaseCost),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (canAfford) MintEmeraldDark else CoralRose
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(benefit, fontSize = 10.5.sp, color = Slate600)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val success = onBuyEquipment(asset)
                                        feedbackMessage = if (success) "✅ Berhasil membeli ${asset.displayName}!" else "⚠️ Saldo kas belum cukup!"
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text(
                                        text = if (canAfford) "Beli & Pasang Sekarang" else "Kas Belum Cukup",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("REKRUTMEN TIM BARU", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate500)
                    }

                    item {
                        val hireCost = 500_000.0
                        val canHire = engine.cash >= hireCost
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MintTintBg.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MintTintBorder)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("👋 Rekrut Operator Baru", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                                        Text("Upah Rp90.000/hari • Siap ditempatkan", fontSize = 10.5.sp, color = Slate600)
                                    }
                                    Text(formatMoney(hireCost), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MintEmeraldDark)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val names = listOf("Rudi", "Nurul", "Agus", "Wati")
                                        val nextName = names[engine.workerList.size % names.size]
                                        val success = onHireStaff(nextName, StationPhase.SETRIKA)
                                        feedbackMessage = if (success) "✅ Berhasil merekrut $nextName!" else "⚠️ Kas tidak cukup!"
                                    },
                                    enabled = canHire,
                                    colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Rekrut Staf Sekarang", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LavenderTintBorder), width = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("lab_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧪", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("LAB EKSPERIMEN SANDBOX", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = BerryLavender)
                            Text("Simulasi Keputusan 7 Hari", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LabToggleRow("Beli Dryer Gas (Rp9.5jt)", "Hilangkan risiko hujan & percepat alur", testBuyDryer) { testBuyDryer = it }
                LabToggleRow("Rekrut 1 Staf Setrika", "Atasi antrean setrika uap", testHireStaff) { testHireStaff = it }
                LabToggleRow("Jalankan Lembur 7 Hari", "Operasi ekstra untuk kejar pesanan", testOvertime) { testOvertime = it }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = LavenderTintBg.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LavenderTintBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PROYEKSI LABA 7 HARI KE DEPAN:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = BerryLavenderDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kondisi Saat Ini", fontSize = 11.5.sp, color = Slate600)
                            Text(formatMoney(baseline7DayProfit), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Setelah Keputusan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BerryLavenderDark)
                            Text(
                                text = formatMoney(exp7DayProfit),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (exp7DayProfit >= baseline7DayProfit) MintEmeraldDark else CoralRose
                            )
                        }
                        if (investCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Modal Investasi Awal: ${formatMoney(investCost)}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = CoralRoseDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Batal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate600)
                    }

                    Button(
                        onClick = { onApplyDecisionToActive(testBuyDryer, testHireStaff, testOvertime) },
                        colors = ButtonDefaults.buttonColors(containerColor = BerryLavender),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).height(42.dp).testTag("btn_apply_lab")
                    ) {
                        Text("Terapkan ke Usaha", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun LabToggleRow(label: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Slate50)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(subtitle, fontSize = 10.sp, color = Slate500)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BerryLavender)
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkyTintBorder), width = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("onboarding_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "PANDUAN MULAI CEPAT (${step}/3)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary, letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (step) {
                    1 -> {
                        Text("1. Kelola Studio 3D & Kecepatan Waktu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Amati alur laundry 3D isometrik Anda dari meja Masuk, Cuci, Jemur/Dryer, Setrika, Packing, hingga Selesai. Gunakan tombol kecepatan [1x] [2x] [5x] untuk mengatur waktu.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, lineHeight = 18.sp)
                        )
                    }
                    2 -> {
                        Text("2. Ketuk Stasiun 3D untuk Tap-Boost & Upgrade", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ketuk langsung mesin 3D atau pilih nama stasiun di bagian bawah untuk mempercepat pekerjaan (+XP) sekaligus membuka panel penugasan staf.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, lineHeight = 18.sp)
                        )
                    }
                    3 -> {
                        Text("3. Sinkronkan ke Simulator Bisnis & ERP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Gunakan bar di bagian atas layar untuk berpindah ke Kalkulator BEP, Unit Economics, dan Simulator What-If kapan saja!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, lineHeight = 18.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (step > 1) {
                        TextButton(onClick = { step-- }) {
                            Text("Kembali", color = Slate600, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 3) step++ else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (step < 3) "Lanjut" else "Mulai Sekarang!", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
