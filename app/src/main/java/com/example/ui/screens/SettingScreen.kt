package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.BusinessConfig
import com.example.ui.theme.*
import com.example.viewmodel.CockpitUiState

@Composable
fun SettingScreen(
    state: CockpitUiState,
    onSaveConfig: (BusinessConfig) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    var businessName by remember(state.config) { mutableStateOf(state.config.businessName) }
    var regularPrice by remember(state.config) { mutableStateOf(state.config.regularPricePerKg.toLong().toString()) }
    var expressPrice by remember(state.config) { mutableStateOf(state.config.expressPricePerKg.toLong().toString()) }
    var targetKg by remember(state.config) { mutableStateOf(state.config.targetKgPerDay.toInt().toString()) }
    var operatingHours by remember(state.config) { mutableStateOf(state.config.operatingHoursPerDay.toInt().toString()) }
    var efficiency by remember(state.config) { mutableStateOf((state.config.operatingEfficiency * 100).toInt().toString()) }

    var washingCount by remember(state.config) { mutableStateOf(state.config.washingMachineCount.toString()) }
    var washingCapacity by remember(state.config) { mutableStateOf(state.config.washingCapacityPerMachineKg.toInt().toString()) }
    var washingCycle by remember(state.config) { mutableStateOf(state.config.washingCycleDurationMinutes.toInt().toString()) }

    var dryerCount by remember(state.config) { mutableStateOf(state.config.dryerCount.toString()) }
    var dryerCapacity by remember(state.config) { mutableStateOf(state.config.dryerCapacityPerMachineKg.toInt().toString()) }
    var dryerCycle by remember(state.config) { mutableStateOf(state.config.dryerCycleDurationMinutes.toInt().toString()) }

    var ironCount by remember(state.config) { mutableStateOf(state.config.ironCount.toString()) }
    var ironCapacityRate by remember(state.config) { mutableStateOf(state.config.ironCapacityPerUnitPerHourKg.toInt().toString()) }

    var staffCount by remember(state.config) { mutableStateOf(state.config.staffCount.toString()) }
    var salaryPerStaff by remember(state.config) { mutableStateOf(state.config.salaryPerStaffMonthly.toLong().toString()) }

    var rent by remember(state.config) { mutableStateOf(state.config.rentMonthly.toLong().toString()) }
    var electricity by remember(state.config) { mutableStateOf(state.config.electricityMonthlyBase.toLong().toString()) }
    var water by remember(state.config) { mutableStateOf(state.config.waterMonthlyBase.toLong().toString()) }
    var chemical by remember(state.config) { mutableStateOf(state.config.chemicalPerKg.toLong().toString()) }
    var initialCash by remember(state.config) { mutableStateOf(state.config.initialCash.toLong().toString()) }

    var showSavedSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 96.dp)
    ) {
        // Header
        Text(
            text = "PENGATURAN USAHA",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = OceanBluePrimary,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Parameter & Biaya Dasar",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Slate900
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Profil & Target
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("PROFIL USAHA & TARGET", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nama Usaha Laundry") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = regularPrice,
                        onValueChange = { regularPrice = it },
                        label = { Text("Harga Reguler/kg (Rp)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = expressPrice,
                        onValueChange = { expressPrice = it },
                        label = { Text("Harga Express/kg (Rp)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetKg,
                        onValueChange = { targetKg = it },
                        label = { Text("Target kg/hari") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = operatingHours,
                        onValueChange = { operatingHours = it },
                        label = { Text("Jam Kerja/hari") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = efficiency,
                        onValueChange = { efficiency = it },
                        label = { Text("Efisiensi (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Mesin Cuci & Dryer & Setrika
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("SPESIFIKASI MESIN & ALAT", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
                Spacer(modifier = Modifier.height(10.dp))

                Text("Mesin Cuci", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = washingCount, onValueChange = { washingCount = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = washingCapacity, onValueChange = { washingCapacity = it }, label = { Text("Kapasitas (kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = washingCycle, onValueChange = { washingCycle = it }, label = { Text("Cycle (mnt)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Dryer / Pengering", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dryerCount, onValueChange = { dryerCount = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = dryerCapacity, onValueChange = { dryerCapacity = it }, label = { Text("Kapasitas (kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = dryerCycle, onValueChange = { dryerCycle = it }, label = { Text("Cycle (mnt)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Meja Setrika", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = ironCount, onValueChange = { ironCount = it }, label = { Text("Unit Setrika") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = ironCapacityRate, onValueChange = { ironCapacityRate = it }, label = { Text("Rate (kg/jam)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Karyawan & Biaya Operasional
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("BIAYA TETAP & VARIABEL", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = staffCount, onValueChange = { staffCount = it }, label = { Text("Jumlah Staf") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = salaryPerStaff, onValueChange = { salaryPerStaff = it }, label = { Text("Gaji/Orang (Rp)") }, modifier = Modifier.weight(1.5f), shape = RoundedCornerShape(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Sewa/Bulan (Rp)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = electricity, onValueChange = { electricity = it }, label = { Text("Listrik/Bulan (Rp)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = water, onValueChange = { water = it }, label = { Text("Air/Bulan (Rp)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = chemical, onValueChange = { chemical = it }, label = { Text("Bahan Kimia/kg (Rp)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = initialCash,
                    onValueChange = { initialCash = it },
                    label = { Text("Modal Kas Saat Ini (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Save Button
        Button(
            onClick = {
                val updatedConfig = state.config.copy(
                    businessName = businessName.ifBlank { "Laundry Bersih" },
                    regularPricePerKg = (regularPrice.toDoubleOrNull() ?: state.config.regularPricePerKg).coerceAtLeast(1_000.0),
                    expressPricePerKg = (expressPrice.toDoubleOrNull() ?: state.config.expressPricePerKg).coerceAtLeast(1_000.0),
                    targetKgPerDay = (targetKg.toDoubleOrNull() ?: state.config.targetKgPerDay).coerceAtLeast(1.0),
                    operatingHoursPerDay = (operatingHours.toDoubleOrNull() ?: state.config.operatingHoursPerDay).coerceIn(1.0, 24.0),
                    operatingEfficiency = ((efficiency.toDoubleOrNull() ?: (state.config.operatingEfficiency * 100)) / 100.0).coerceIn(0.1, 1.0),
                    washingMachineCount = (washingCount.toIntOrNull() ?: state.config.washingMachineCount).coerceAtLeast(1),
                    washingCapacityPerMachineKg = (washingCapacity.toDoubleOrNull() ?: state.config.washingCapacityPerMachineKg).coerceAtLeast(1.0),
                    washingCycleDurationMinutes = (washingCycle.toDoubleOrNull() ?: state.config.washingCycleDurationMinutes).coerceAtLeast(1.0),
                    dryerCount = (dryerCount.toIntOrNull() ?: state.config.dryerCount).coerceAtLeast(0),
                    dryerCapacityPerMachineKg = (dryerCapacity.toDoubleOrNull() ?: state.config.dryerCapacityPerMachineKg).coerceAtLeast(1.0),
                    dryerCycleDurationMinutes = (dryerCycle.toDoubleOrNull() ?: state.config.dryerCycleDurationMinutes).coerceAtLeast(1.0),
                    ironCount = (ironCount.toIntOrNull() ?: state.config.ironCount).coerceAtLeast(1),
                    ironCapacityPerUnitPerHourKg = (ironCapacityRate.toDoubleOrNull() ?: state.config.ironCapacityPerUnitPerHourKg).coerceAtLeast(1.0),
                    staffCount = (staffCount.toIntOrNull() ?: state.config.staffCount).coerceAtLeast(1),
                    salaryPerStaffMonthly = (salaryPerStaff.toDoubleOrNull() ?: state.config.salaryPerStaffMonthly).coerceAtLeast(0.0),
                    rentMonthly = (rent.toDoubleOrNull() ?: state.config.rentMonthly).coerceAtLeast(0.0),
                    electricityMonthlyBase = (electricity.toDoubleOrNull() ?: state.config.electricityMonthlyBase).coerceAtLeast(0.0),
                    waterMonthlyBase = (water.toDoubleOrNull() ?: state.config.waterMonthlyBase).coerceAtLeast(0.0),
                    chemicalPerKg = (chemical.toDoubleOrNull() ?: state.config.chemicalPerKg).coerceAtLeast(0.0),
                    initialCash = (initialCash.toDoubleOrNull() ?: state.config.initialCash).coerceAtLeast(0.0)
                )
                onSaveConfig(updatedConfig)
                showSavedSnackbar = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_config")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Perubahan Parameter", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset to default sample data button
        OutlinedButton(
            onClick = onResetToDefault,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_reset_default")
        ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = DangerRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset ke Data Default Laundry Bersih", fontWeight = FontWeight.Bold, color = DangerRed)
        }

        if (showSavedSnackbar) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreenBg),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SuccessGreenBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parameter berhasil diperbarui dan disinkronkan ke Cockpit!", color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
