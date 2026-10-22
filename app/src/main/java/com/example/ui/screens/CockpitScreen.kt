package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.CockpitUiState

@Composable
fun CockpitScreen(
    state: CockpitUiState,
    onOpenAddOrder: () -> Unit,
    onNavigateToOperasi: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = remember(state) {
        BusinessEngine.computeDailySummary(state)
    }
    val unitEco = remember(state.config) {
        BusinessEngine.computeUnitEconomics(state.config)
    }
    val bep = remember(state.config) {
        BusinessEngine.computeBEP(state.config)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 96.dp)
    ) {
        // App Title & Store Name Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COCKPIT AKTIF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBluePrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Text(
                    text = state.config.businessName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                )
            }

            // Quick Add Button
            Button(
                onClick = onOpenAddOrder,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_quick_add_order")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Order", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kondisi Usaha Hari Ini (Badges)
        Text(
            text = "Kondisi Usaha Hari Ini",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(summary.healthIndicators) { indicator ->
                HealthBadgePill(indicator = indicator)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Alert Card: PERLU PERHATIAN
        AttentionAlertCard(message = summary.attentionMessage)

        Spacer(modifier = Modifier.height(12.dp))

        // Target vs Actual Card
        TargetVsActualCard(
            targetKg = summary.targetKgPerDay,
            actualKg = summary.actualKgPerDay,
            progressPercent = summary.progressPercent
        )

        Spacer(modifier = Modifier.height(14.dp))

        // STATUS HARI INI - Section Header
        Text(
            text = "STATUS HARI INI",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Slate900,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2x2 Metric Grid for Today
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CockpitMetricBox(
                title = "Order Masuk",
                value = "${summary.orderCountToday} order",
                subtitle = "${summary.kgMasukToday.toInt()} kg total",
                icon = Icons.Default.ReceiptLong,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricBox(
                title = "Selesai",
                value = "${summary.kgSelesaiToday.toInt()} kg",
                subtitle = "Belum: ${summary.kgBelumSelesaiToday.toInt()} kg",
                valueColor = SuccessGreen,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CockpitMetricBox(
                title = "Pendapatan",
                value = formatRupiahCompact(summary.revenueToday),
                subtitle = "Biaya: ${formatRupiahCompact(summary.costToday)}",
                valueColor = Slate900,
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )
            CockpitMetricBox(
                title = "Estimasi Laba",
                value = formatRupiahCompact(summary.profitToday),
                subtitle = "Margin: ${summary.marginPercent.toInt()}%",
                valueColor = if (summary.profitToday >= 0) SuccessGreen else DangerRed,
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Kapasitas Terpakai Bar Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kapasitas Terpakai",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${summary.capacityUtilizationPercent.toInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                summary.capacityUtilizationPercent > 90 -> DangerRed
                                summary.capacityUtilizationPercent > 75 -> WarningYellow
                                else -> SuccessGreen
                            }
                        )
                    )
                    Text(
                        text = "Limit praktis: ${state.config.maxDailyPracticalCapacityKg.toInt()} kg/hari",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                    )
                }

                Button(
                    onClick = onNavigateToOperasi,
                    colors = ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Cek Jalur", fontSize = 12.sp, color = OceanBluePrimary)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = OceanBluePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Keputusan Hari Ini
        DecisionRecommendationCard(decisions = summary.dailyDecisions)

        Spacer(modifier = Modifier.height(16.dp))

        // BUSINESS HEALTH KPI SECTION
        Text(
            text = "BUSINESS HEALTH & KPI UTAMA",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Slate900,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                KpiRowItem(label = "Omzet Hari Ini", value = formatRupiah(summary.revenueToday))
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Omzet Bulan Ini (Est.)", value = formatRupiah(summary.revenueToday * 30.0))
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Net Profit Hari Ini", value = formatRupiah(summary.profitToday), isPositive = summary.profitToday >= 0)
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Margin Bersih", value = "${summary.marginPercent.toInt()}%")
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Kas Tersedia", value = formatRupiah(state.config.initialCash))
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Volume Cuci / Hari", value = "${summary.kgMasukToday.toInt()} kg")
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Revenue / kg", value = "${formatRupiah(unitEco.revenuePerKg)}/kg")
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Cost / kg (Full)", value = "${formatRupiah(unitEco.variableCostPerKg + unitEco.fixedCostAllocationPerKg)}/kg")
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Profit / kg", value = "${formatRupiah(unitEco.netProfitPerKg)}/kg", isPositive = unitEco.netProfitPerKg >= 0)
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Average Order Size", value = "${String.format("%.1f", if (summary.orderCountToday > 0) summary.kgMasukToday / summary.orderCountToday else 0.0)} kg")
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 6.dp))
                KpiRowItem(label = "Titik Impas (BEP)", value = "${bep.bepKgPerDay.toInt()} kg/hari")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Simulator Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = OceanBlueDark),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSimulator() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SIMULATOR WHAT-IF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanGlow,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Bagaimana jika order naik atau harga naik?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun KpiRowItem(
    label: String,
    value: String,
    isPositive: Boolean? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = when (isPositive) {
                    true -> SuccessGreen
                    false -> DangerRed
                    null -> Slate900
                }
            )
        )
    }
}
