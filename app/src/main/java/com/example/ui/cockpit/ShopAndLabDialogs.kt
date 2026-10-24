package com.example.ui.cockpit

import androidx.compose.foundation.background
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🏪 TOKO ALAT & REKRUT STAF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary)
                        )
                        Text(
                            text = "Uang Kas: ${formatMoney(engine.cash)}",
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
                        Text("BELI MESIN BARU (BIAR CUCIAN CEPAT SELESAI)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate500)
                    }

                    val shopAssets = listOf(
                        Triple(AssetType.DRYER_GAS_10KG, "Aman walau hujan deras! Baju kering cuma 45 menit.", "Anti-Hujan • Tahap 3"),
                        Triple(AssetType.SETRIKA_UAP_BOILER, "Menyetrika jadi 1,6x lebih cepat dan staf tidak mudah capek.", "Anti-Macet • Tahap 4"),
                        Triple(AssetType.MESIN_CUCI_7KG, "Tambah 1 mesin cuci lagi agar bisa mencuci bersamaan.", "Tambah Mesin • Tahap 2"),
                        Triple(AssetType.MESIN_CUCI_14KG, "Mesin cuci raksasa, muat banyak cucian sekaligus!", "Kapasitas Besar • Tahap 2")
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
                                        feedbackMessage = if (success) "✅ Berhasil membeli ${asset.displayName}!" else "⚠️ Uang kas belum cukup!"
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text(
                                        text = if (canAfford) "🛒 Beli & Pasang Sekarang" else "Uang Kas Belum Cukup",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("TAMBAH PEKERJA BARU", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate500)
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
                                        Text("👋 Rekrut Pekerja Baru", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                                        Text("Gaji Rp90.000/hari • Langsung bantu kerja di ruang 3D!", fontSize = 10.5.sp, color = Slate600)
                                    }
                                    Text(formatMoney(hireCost), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MintEmeraldDark)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val names = listOf("Rudi", "Nurul", "Agus", "Wati")
                                        val nextName = names[engine.workerList.size % names.size]
                                        val success = onHireStaff(nextName, StationPhase.SETRIKA)
                                        feedbackMessage = if (success) "✅ Berhasil merekrut $nextName!" else "⚠️ Uang kas belum cukup!"
                                    },
                                    enabled = canHire,
                                    colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("➕ Rekrut Pekerja Sekarang", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
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
                            Text("LAB PERCOBAAN 7 HARI", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = BerryLavender)
                            Text("Coba Dulu Sebelum Beli!", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LabToggleRow("Beli Dryer Gas (Rp9,5jt)", "Cucian cepat kering walau hujan", testBuyDryer) { testBuyDryer = it }
                LabToggleRow("Rekrut 1 Staf Setrika", "Bantu menyetrika biar tidak menumpuk", testHireStaff) { testHireStaff = it }
                LabToggleRow("Tambah Jam Lembur", "Toko buka 2 jam lebih lama", testOvertime) { testOvertime = it }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = LavenderTintBg.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LavenderTintBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("HASIL PERKIRAAN UNTUNG 7 HARI:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = BerryLavenderDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Untung Sekarang", fontSize = 11.5.sp, color = Slate600)
                            Text(formatMoney(baseline7DayProfit), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Untung Setelah Dicoba", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BerryLavenderDark)
                            Text(
                                text = formatMoney(exp7DayProfit),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (exp7DayProfit >= baseline7DayProfit) MintEmeraldDark else CoralRose
                            )
                        }
                        if (investCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Uang yang Dibutuhkan: ${formatMoney(investCost)}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = CoralRoseDark)
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
                        Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate600)
                    }

                    Button(
                        onClick = { onApplyDecisionToActive(testBuyDryer, testHireStaff, testOvertime) },
                        colors = ButtonDefaults.buttonColors(containerColor = BerryLavender),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).height(42.dp).testTag("btn_apply_lab")
                    ) {
                        Text("Pasang ke Toko!", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
                    text = "💡 CARA MAIN MUDAH (LANGKAH ${step} DARI 4)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary, letterSpacing = 0.8.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (step) {
                    1 -> {
                        Text("1️⃣ Urutan Cucian: Nomor 1 sampai 6!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Baju kotor pelanggan berjalan berurutan melalui 6 tempat:\n" +
                                "• 1. 🧺 Masuk (Timbang baju kotor)\n" +
                                "• 2. 🫧 Cuci (Masuk mesin cuci)\n" +
                                "• 3. ☀️ Kering (Jemur / Dryer)\n" +
                                "• 4. 💨 Setrika (Biar licin & rapi)\n" +
                                "• 5. 📦 Packing (Lipat & bungkus)\n" +
                                "• 6. ✨ Selesai (Terima uang kas!)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, lineHeight = 18.sp)
                        )
                    }
                    2 -> {
                        Text("2️⃣ Ketuk Alat 3D untuk Bantu Bekerja!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ruang 3D bukan cuma gambar pajangan! Ketuk langsung Mesin Cuci, Jemuran, atau Meja Setrika di Ruang 3D (atau tombol ⚡ Bantu!) untuk mempercepat pekerjaan dan langsung dapat bintang XP!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, lineHeight = 18.sp)
                        )
                    }
                    3 -> {
                        Text("3️⃣ Pakai Kopi, Turbo & Pindahkan Pekerja!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Kalau Budi atau Siti mulai lelah (😴), klik tombol ☕ Kopi! Kalau ada stasiun yang menumpuk, klik tombol 🔧 Atur untuk memindahkan pekerja ke stasiun yang sibuk.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, lineHeight = 18.sp)
                        )
                    }
                    4 -> {
                        Text("4️⃣ Ikuti 4 Tab di Bawah Satu per Satu!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Slate900))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Supaya tidak pusing, layar dibagi rapi menjadi 4 menu bertahap:\n" +
                                "• 1. Ruang 3D: Tempat bermain & pantau toko\n" +
                                "• 2. Alur: Lihat antrean 6 stasiun & daftar order\n" +
                                "• 3. Tim & Alat: Rekrut pekerja, beli mesin & klaim misi\n" +
                                "• 4. Kas & Lab: Lihat tabungan uang, BEP & simulator bisnis!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, lineHeight = 18.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (step > 1) {
                        TextButton(onClick = { step-- }) {
                            Text("⬅️ Sebelumnya", color = Slate600, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 4) step++ else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (step < 4) "Lanjut ➔" else "🚀 Ayo Mulai Main!", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
