package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.*
import com.example.ui.components.AttentionAlertCard
import com.example.ui.theme.*
import com.example.viewmodel.CockpitUiState
import kotlin.math.roundToInt

@Composable
fun SimulatorScreen(
    state: CockpitUiState,
    onUpdateOrderKg: (Double) -> Unit,
    onUpdatePrice: (Double) -> Unit,
    onUpdateStaff: (Int) -> Unit,
    onUpdateWashingMachines: (Int) -> Unit,
    onUpdateDryers: (Int) -> Unit,
    onUpdateRent: (Double) -> Unit,
    onUpdateElectricity: (Double) -> Unit,
    onUpdateSalary: (Double) -> Unit,
    onApplyScenario: (ScenarioPreset) -> Unit,
    onUpdateMachineInvestmentInput: (MachineInvestmentInput) -> Unit,
    onUpdateTargetProfit: (Double) -> Unit,
    onUpdatePricingMargin: (Double) -> Unit,
    onUpdatePricingCompetitor: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val simResult = remember(
        state.simOrderKgPerDay,
        state.simPricePerKg,
        state.simStaffCount,
        state.simWashingMachines,
        state.simDryers,
        state.simRentMonthly,
        state.simElectricityMonthly,
        state.simSalaryPerStaff,
        state.config
    ) {
        BusinessEngine.computeSimulator(state)
    }

    val machineInvestResult = remember(state.config, state.machineInvestmentInput) {
        BusinessEngine.computeMachineInvestment(state.config, state.machineInvestmentInput)
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: What-If & Skenario, 1: Investasi Mesin, 2: Target Profit & Pricing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 96.dp)
    ) {
        // Header
        Text(
            text = "BUSINESS SIMULATOR",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = OceanBluePrimary,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "What-If & Skenario Bisnis",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Slate900
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = OceanBluePrimary,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("What-If Slider", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Beli Mesin?", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Target & Harga", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> WhatIfTabContent(
                state = state,
                simResult = simResult,
                onUpdateOrderKg = onUpdateOrderKg,
                onUpdatePrice = onUpdatePrice,
                onUpdateStaff = onUpdateStaff,
                onUpdateWashingMachines = onUpdateWashingMachines,
                onUpdateDryers = onUpdateDryers,
                onUpdateRent = onUpdateRent,
                onUpdateElectricity = onUpdateElectricity,
                onUpdateSalary = onUpdateSalary,
                onApplyScenario = onApplyScenario
            )
            1 -> MachineInvestmentTabContent(
                state = state,
                result = machineInvestResult,
                onUpdateInput = onUpdateMachineInvestmentInput
            )
            2 -> TargetAndPricingTabContent(
                state = state,
                onUpdateTargetProfit = onUpdateTargetProfit,
                onUpdatePricingMargin = onUpdatePricingMargin,
                onUpdatePricingCompetitor = onUpdatePricingCompetitor
            )
        }
    }
}

@Composable
fun WhatIfTabContent(
    state: CockpitUiState,
    simResult: SimulatorCalculations,
    onUpdateOrderKg: (Double) -> Unit,
    onUpdatePrice: (Double) -> Unit,
    onUpdateStaff: (Int) -> Unit,
    onUpdateWashingMachines: (Int) -> Unit,
    onUpdateDryers: (Int) -> Unit,
    onUpdateRent: (Double) -> Unit,
    onUpdateElectricity: (Double) -> Unit,
    onUpdateSalary: (Double) -> Unit,
    onApplyScenario: (ScenarioPreset) -> Unit
) {
    // SCENARIO PRESET BUTTONS (Horizontal Chips)
    Text(
        text = "SKENARIO CEPAT",
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Slate500,
            letterSpacing = 0.5.sp
        )
    )
    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(ScenarioPreset.values()) { preset ->
            val isSelected = preset == state.activeScenario
            FilterChip(
                selected = isSelected,
                onClick = { onApplyScenario(preset) },
                label = { Text(preset.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OceanBluePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }

    // Impact Banner if Scenario Applied
    if (state.scenarioImpactText != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = InfoBlueBg),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(InfoBlueBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.scenarioImpactText,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontWeight = FontWeight.Medium)
                )
            }
        }
    }

    if (state.scenarioWarningText != null || simResult.isOverCapacity) {
        Spacer(modifier = Modifier.height(8.dp))
        AttentionAlertCard(
            message = state.scenarioWarningText ?: "Utilisasi kapasitas mencapai ${simResult.capacityUtilizationPercent.toInt()}%! Mesin dan karyawan tidak akan mampu menyelesaikan tepat waktu."
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // LIVE RESULT COCKPIT CARD (Sticky Summary feel)
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simulator_live_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HASIL PROYEKSI LIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanGlow,
                        letterSpacing = 1.sp
                    )
                )

                // Overcapacity / Safe Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (simResult.isOverCapacity) DangerRed else SuccessGreen)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (simResult.isOverCapacity) "OVERLOAD" else "UTILISASI ${simResult.capacityUtilizationPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Omzet / Bulan", style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp))
                    Text(
                        text = formatRupiahCompact(simResult.monthlyRevenue),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Laba Bersih / Bulan", style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp))
                    Text(
                        text = formatRupiahCompact(simResult.monthlyProfit),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (simResult.monthlyProfit >= 0) SuccessGreen else DangerRed,
                            fontSize = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate700)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SimStatSubItem(label = "Margin", value = "${simResult.marginPercent.toInt()}%")
                SimStatSubItem(label = "BEP Harian", value = "${simResult.bepKgPerDay.toInt()} kg")
                SimStatSubItem(label = "Total Biaya", value = formatRupiahCompact(simResult.totalMonthlyCost))
                SimStatSubItem(label = "Cashflow", value = formatRupiahCompact(simResult.monthlyCashflow))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // SLIDERS SECTION
    Text(
        text = "PARAMETER SIMULASI (GESER SLIDER)",
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Slate900,
            letterSpacing = 0.5.sp
        )
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Slider 1: Order per hari (30 - 250 kg)
            SliderItem(
                label = "ORDER PER HARI",
                currentDisplay = "${state.simOrderKgPerDay.toInt()} kg/hari",
                value = state.simOrderKgPerDay.toFloat(),
                range = 30f..250f,
                steps = 22,
                onValueChange = { onUpdateOrderKg(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 2: Harga / kg (Rp 6.000 - Rp 20.000)
            SliderItem(
                label = "HARGA JUAL / KG",
                currentDisplay = "${formatRupiah(state.simPricePerKg)}/kg",
                value = state.simPricePerKg.toFloat(),
                range = 6_000f..20_000f,
                steps = 28,
                onValueChange = { onUpdatePrice(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 3: Karyawan (1 - 10)
            SliderItem(
                label = "JUMLAH KARYAWAN",
                currentDisplay = "${state.simStaffCount} orang",
                value = state.simStaffCount.toFloat(),
                range = 1f..10f,
                steps = 9,
                onValueChange = { onUpdateStaff(it.roundToInt()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 4: Mesin Cuci (1 - 10)
            SliderItem(
                label = "JUMLAH MESIN CUCI",
                currentDisplay = "${state.simWashingMachines} unit",
                value = state.simWashingMachines.toFloat(),
                range = 1f..10f,
                steps = 9,
                onValueChange = { onUpdateWashingMachines(it.roundToInt()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 5: Dryer (0 - 10)
            SliderItem(
                label = "JUMLAH DRYER",
                currentDisplay = "${state.simDryers} unit",
                value = state.simDryers.toFloat(),
                range = 0f..10f,
                steps = 10,
                onValueChange = { onUpdateDryers(it.roundToInt()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 6: Sewa Tempat (0 - 30 jt)
            SliderItem(
                label = "SEWA BULANAN",
                currentDisplay = formatRupiahCompact(state.simRentMonthly),
                value = state.simRentMonthly.toFloat(),
                range = 0f..30_000_000f,
                steps = 30,
                onValueChange = { onUpdateRent(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 7: Listrik Bulanan (1jt - 10jt)
            SliderItem(
                label = "LISTRIK BULANAN",
                currentDisplay = formatRupiahCompact(state.simElectricityMonthly),
                value = state.simElectricityMonthly.toFloat(),
                range = 1_000_000f..10_000_000f,
                steps = 18,
                onValueChange = { onUpdateElectricity(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            // Slider 8: Gaji per orang (1.5jt - 7jt)
            SliderItem(
                label = "GAJI PER ORANG",
                currentDisplay = formatRupiah(state.simSalaryPerStaff),
                value = state.simSalaryPerStaff.toFloat(),
                range = 1_500_000f..7_000_000f,
                steps = 22,
                onValueChange = { onUpdateSalary(it.toDouble()) }
            )
        }
    }
}

@Composable
fun MachineInvestmentTabContent(
    state: CockpitUiState,
    result: MachineInvestmentResult,
    onUpdateInput: (MachineInvestmentInput) -> Unit
) {
    val input = state.machineInvestmentInput

    Card(
        colors = CardDefaults.cardColors(containerColor = if (result.isRecommended) Color(0xFFECFDF5) else WarningYellowBg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (result.isRecommended) SuccessGreenBorder else WarningYellowBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.isRecommended) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.isRecommended) SuccessGreen else WarningYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.recommendationTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.isRecommended) Color(0xFF065F46) else Color(0xFF92400E)
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result.recommendationReason,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate800,
                    lineHeight = 18.sp
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Form Input Mesin Baru
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SIMULATOR PEMBELIAN MESIN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = input.machinePrice.toLong().toString(),
                onValueChange = {
                    val p = it.toDoubleOrNull() ?: input.machinePrice
                    onUpdateInput(input.copy(machinePrice = p))
                },
                label = { Text("Harga Mesin (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input.capacityKg.toInt().toString(),
                    onValueChange = {
                        val c = it.toDoubleOrNull() ?: input.capacityKg
                        onUpdateInput(input.copy(capacityKg = c))
                    },
                    label = { Text("Kapasitas (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = input.cycleMinutes.toInt().toString(),
                    onValueChange = {
                        val cyc = it.toDoubleOrNull() ?: input.cycleMinutes
                        onUpdateInput(input.copy(cycleMinutes = cyc))
                    },
                    label = { Text("Cycle (mnt)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = input.additionalElectricityMonthly.toLong().toString(),
                onValueChange = {
                    val el = it.toDoubleOrNull() ?: input.additionalElectricityMonthly
                    onUpdateInput(input.copy(additionalElectricityMonthly = el))
                },
                label = { Text("Tambahan Listrik/Bulan (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Output Metrics
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(10.dp))

            Text("PROYEKSI KELAYAKAN INVESTASI", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tambahan Kapasitas", style = MaterialTheme.typography.bodySmall)
                Text("+${result.additionalDailyCapacityKg.toInt()} kg/hari", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Potensi Omzet Tambahan", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(result.additionalMonthlyRevenuePotential), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tambahan Laba Bersih", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(result.additionalMonthlyNetProfit), fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Payback Period", style = MaterialTheme.typography.bodySmall)
                Text("${String.format("%.1f", result.paybackPeriodMonths)} Bulan", fontWeight = FontWeight.ExtraBold, color = OceanBlueDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ROI Tahunan", style = MaterialTheme.typography.bodySmall)
                Text("${result.annualROI.toInt()}% / tahun", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TargetAndPricingTabContent(
    state: CockpitUiState,
    onUpdateTargetProfit: (Double) -> Unit,
    onUpdatePricingMargin: (Double) -> Unit,
    onUpdatePricingCompetitor: (Double) -> Unit
) {
    val config = state.config

    // 1. Target Profit Calculator
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "KALKULATOR TARGET PROFIT BULANAN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.targetProfitMonthly.toLong().toString(),
                onValueChange = {
                    val v = it.toDoubleOrNull() ?: state.targetProfitMonthly
                    onUpdateTargetProfit(v)
                },
                label = { Text("Target Laba Bersih / Bulan (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Formula: Required kg = (Monthly Fixed Cost + Target Profit) / Contribution Margin per kg
            val contributionMargin = (config.regularPricePerKg - config.variableCostPerKg).coerceAtLeast(100.0)
            val neededKgMonthly = (config.totalPureFixedCostMonthly + state.targetProfitMonthly) / contributionMargin
            val neededKgDaily = neededKgMonthly / 30.0
            val avgOrderWeight = 6.0
            val neededOrdersDaily = neededKgDaily / avgOrderWeight
            val neededMachines = (neededKgDaily / (config.washingCapacityPerMachineKg * 10.0)).toInt().coerceAtLeast(1)
            val neededStaff = (neededKgDaily / 30.0).toInt().coerceAtLeast(1)

            Text("Untuk mencapai target ${formatRupiah(state.targetProfitMonthly)}/bulan:", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dibutuhkan per Bulan", style = MaterialTheme.typography.bodySmall)
                Text("${neededKgMonthly.toInt()} kg/bulan", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dibutuhkan per Hari", style = MaterialTheme.typography.bodySmall)
                Text("${neededKgDaily.toInt()} kg/hari", fontWeight = FontWeight.Bold, color = OceanBluePrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order Rata-rata", style = MaterialTheme.typography.bodySmall)
                Text("${neededOrdersDaily.toInt()} order/hari", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kebutuhan Mesin Cuci", style = MaterialTheme.typography.bodySmall)
                Text("$neededMachines unit", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kebutuhan Karyawan", style = MaterialTheme.typography.bodySmall)
                Text("$neededStaff orang", fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Pricing Simulator
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PRICING SIMULATOR",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
            )
            Text(
                text = "Berapa harga yang seharusnya saya pasang?",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Full cost per kg
            val unitEco = BusinessEngine.computeUnitEconomics(config)
            val fullCostPerKg = unitEco.variableCostPerKg + unitEco.fixedCostAllocationPerKg

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.pricingTargetMarginPercent.toInt().toString(),
                    onValueChange = {
                        val m = it.toDoubleOrNull() ?: state.pricingTargetMarginPercent
                        onUpdatePricingMargin(m)
                    },
                    label = { Text("Target Margin (%)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = state.pricingCompetitorPrice.toLong().toString(),
                    onValueChange = {
                        val cp = it.toDoubleOrNull() ?: state.pricingCompetitorPrice
                        onUpdatePricingCompetitor(cp)
                    },
                    label = { Text("Harga Kompetitor") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val minViablePrice = fullCostPerKg * 1.10 // 10% safety margin
            val recommendedPrice = fullCostPerKg / (1.0 - (state.pricingTargetMarginPercent / 100.0)).coerceAtLeast(0.1)
            val targetPrice = maxOf(recommendedPrice, state.pricingCompetitorPrice * 0.95)

            Text("ANALISIS REKOMENDASI HARGA", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cost / kg (Full HPP)", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(fullCostPerKg), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga Minimum (Viable)", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(minViablePrice), fontWeight = FontWeight.Bold, color = DangerRed)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga Sehat (Recommended)", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(recommendedPrice), fontWeight = FontWeight.Bold, color = SuccessGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga Target Bersaing", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(targetPrice), fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary)
            }
        }
    }
}

@Composable
fun SliderItem(
    label: String,
    currentDisplay: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = currentDisplay,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = OceanBlueDark
                )
            )
        }

        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = OceanBluePrimary,
                activeTrackColor = OceanBluePrimary,
                inactiveTrackColor = Slate200
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SimStatSubItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 10.sp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp))
    }
}
