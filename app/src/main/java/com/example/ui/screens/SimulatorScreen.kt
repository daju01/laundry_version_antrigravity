package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 96.dp)
    ) {
        Text(
            text = "BUSINESS SIMULATOR",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BerryLavender,
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

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = BerryLavender,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("What-If Slider", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Beli Mesin?", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Target & Harga", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
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
                label = { Text(preset.label, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BerryLavender,
                    selectedLabelColor = Color.White
                )
            )
        }
    }

    if (state.scenarioImpactText != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = InfoBlueBg),
            shape = RoundedCornerShape(12.dp),
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

    // LIVE RESULT COCKPIT CARD (Vibrant Ocean-to-Teal Gradient Card)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(OceanBlueDark, OceanBluePrimary, TealAccent)))
            .testTag("simulator_live_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ HASIL PROYEKSI LIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFEF08A),
                        letterSpacing = 1.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (simResult.isOverCapacity) CoralRose else Color.White.copy(alpha = 0.22f))
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
                    Text(text = "Omzet / Bulan", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp))
                    Text(
                        text = formatRupiahCompact(simResult.monthlyRevenue),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 21.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Laba Bersih / Bulan", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp))
                    Text(
                        text = formatRupiahCompact(simResult.monthlyProfit),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = if (simResult.monthlyProfit >= 0) Color(0xFFFEF08A) else Color(0xFFFECDD3),
                            fontSize = 21.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SliderItem(
                label = "ORDER PER HARI",
                currentDisplay = "${state.simOrderKgPerDay.toInt()} kg/hari",
                value = state.simOrderKgPerDay.toFloat(),
                range = 30f..250f,
                steps = 22,
                onValueChange = { onUpdateOrderKg(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            SliderItem(
                label = "HARGA JUAL / KG",
                currentDisplay = "${formatRupiah(state.simPricePerKg)}/kg",
                value = state.simPricePerKg.toFloat(),
                range = 6_000f..20_000f,
                steps = 28,
                onValueChange = { onUpdatePrice(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            SliderItem(
                label = "JUMLAH KARYAWAN",
                currentDisplay = "${state.simStaffCount} orang",
                value = state.simStaffCount.toFloat(),
                range = 1f..10f,
                steps = 9,
                onValueChange = { onUpdateStaff(it.roundToInt()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            SliderItem(
                label = "JUMLAH MESIN CUCI",
                currentDisplay = "${state.simWashingMachines} unit",
                value = state.simWashingMachines.toFloat(),
                range = 1f..10f,
                steps = 9,
                onValueChange = { onUpdateWashingMachines(it.roundToInt()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            SliderItem(
                label = "JUMLAH DRYER",
                currentDisplay = "${state.simDryers} unit",
                value = state.simDryers.toFloat(),
                range = 0f..10f,
                steps = 10,
                onValueChange = { onUpdateDryers(it.roundToInt()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            SliderItem(
                label = "SEWA BULANAN",
                currentDisplay = formatRupiahCompact(state.simRentMonthly),
                value = state.simRentMonthly.toFloat(),
                range = 0f..30_000_000f,
                steps = 30,
                onValueChange = { onUpdateRent(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

            SliderItem(
                label = "LISTRIK BULANAN",
                currentDisplay = formatRupiahCompact(state.simElectricityMonthly),
                value = state.simElectricityMonthly.toFloat(),
                range = 1_000_000f..10_000_000f,
                steps = 18,
                onValueChange = { onUpdateElectricity(it.toDouble()) }
            )

            HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

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
        colors = CardDefaults.cardColors(containerColor = if (result.isRecommended) MintTintBg else SunnyTintBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (result.isRecommended) MintTintBorder else SunnyTintBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.isRecommended) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.isRecommended) MintEmeraldDark else SunnyAmberDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.recommendationTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.isRecommended) MintEmeraldDark else SunnyAmberDark
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

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SIMULATOR PEMBELIAN MESIN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = Slate900)
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
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(10.dp))

            Text("PROYEKSI KELAYAKAN INVESTASI", style = MaterialTheme.typography.labelSmall.copy(color = OceanBlueDeep, fontWeight = FontWeight.ExtraBold))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tambahan Kapasitas", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("+${result.additionalDailyCapacityKg.toInt()} kg/hari", fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Potensi Omzet Tambahan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text(formatRupiah(result.additionalMonthlyRevenuePotential), fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tambahan Laba Bersih", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text(formatRupiah(result.additionalMonthlyNetProfit), fontWeight = FontWeight.ExtraBold, color = MintEmeraldDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Payback Period", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("${String.format("%.1f", result.paybackPeriodMonths)} Bulan", fontWeight = FontWeight.ExtraBold, color = OceanBlueDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ROI Tahunan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("${result.annualROI.toInt()}% / tahun", fontWeight = FontWeight.Bold, color = Slate900)
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

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "KALKULATOR TARGET PROFIT BULANAN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = Slate900)
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

            val contributionMargin = (config.regularPricePerKg - config.variableCostPerKg).coerceAtLeast(100.0)
            val neededKgMonthly = (config.totalPureFixedCostMonthly + state.targetProfitMonthly) / contributionMargin
            val neededKgDaily = neededKgMonthly / 30.0
            val avgOrderWeight = 6.0
            val neededOrdersDaily = neededKgDaily / avgOrderWeight
            val neededMachines = (neededKgDaily / (config.washingCapacityPerMachineKg.coerceAtLeast(1.0) * 10.0)).toInt().coerceAtLeast(1)
            val neededStaff = (neededKgDaily / 30.0).toInt().coerceAtLeast(1)

            Text("Untuk mencapai target ${formatRupiah(state.targetProfitMonthly)}/bulan:", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dibutuhkan per Bulan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("${neededKgMonthly.toInt()} kg/bulan", fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dibutuhkan per Hari", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("${neededKgDaily.toInt()} kg/hari", fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order Rata-rata", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("${neededOrdersDaily.toInt()} order/hari", fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kebutuhan Mesin Cuci", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("$neededMachines unit", fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kebutuhan Karyawan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text("$neededStaff orang", fontWeight = FontWeight.Bold, color = Slate900)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PRICING SIMULATOR",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = Slate900)
            )
            Text(
                text = "Berapa harga yang seharusnya saya pasang?",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )
            Spacer(modifier = Modifier.height(10.dp))

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

            val minViablePrice = fullCostPerKg * 1.10
            val recommendedPrice = fullCostPerKg / (1.0 - (state.pricingTargetMarginPercent / 100.0)).coerceAtLeast(0.1)
            val targetPrice = maxOf(recommendedPrice, state.pricingCompetitorPrice * 0.95)

            Text("ANALISIS REKOMENDASI HARGA", style = MaterialTheme.typography.labelSmall.copy(color = OceanBlueDeep, fontWeight = FontWeight.ExtraBold))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cost / kg (Full HPP)", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text(formatRupiah(fullCostPerKg), fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga Minimum (Viable)", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text(formatRupiah(minViablePrice), fontWeight = FontWeight.Bold, color = CoralRose)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga Sehat (Recommended)", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Text(formatRupiah(recommendedPrice), fontWeight = FontWeight.ExtraBold, color = MintEmeraldDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harga Target Bersaing", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
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
                inactiveTrackColor = SkyTintBg
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SimStatSubItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.80f), fontSize = 10.sp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp))
    }
}
