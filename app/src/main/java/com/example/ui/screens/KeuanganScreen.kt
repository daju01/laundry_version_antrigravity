package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.CockpitUiState

@Composable
fun KeuanganScreen(
    state: CockpitUiState,
    modifier: Modifier = Modifier
) {
    val config = state.config
    val unitEco = remember(config) {
        BusinessEngine.computeUnitEconomics(config)
    }
    val actualDailyKg = remember(state.orders) {
        val nonTaken = state.orders.filter { it.status != OrderStatus.DIAMBIL }
        if (nonTaken.isNotEmpty()) nonTaken.sumOf { it.weightKg } else config.targetKgPerDay
    }
    val bep = remember(config, actualDailyKg) {
        BusinessEngine.computeBEP(config, actualDailyKg)
    }
    val cashflowForecast = remember(config) {
        val dailyProfit = (config.targetKgPerDay * config.regularPricePerKg) -
                ((config.totalPureFixedCostMonthly / 30.0) + (config.variableCostPerKg * config.targetKgPerDay))
        BusinessEngine.computeCashflowForecast(config, dailyProfit)
    }
    val custEco = remember(state.orders, config) {
        BusinessEngine.computeCustomerEconomics(state.orders.size, config.regularPricePerKg)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 96.dp)
    ) {
        // Header
        Text(
            text = "ANALISIS KEUANGAN & PROFITABILITAS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = OceanBluePrimary,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Unit Economics & Cashflow",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Slate900
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 9. UNIT ECONOMICS - 4 Big Cards
        Text(
            text = "UNIT ECONOMICS (PER KILOGRAM)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Slate900,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UnitEcoCard(
                title = "HARGA JUAL",
                amount = formatRupiah(unitEco.revenuePerKg) + "/kg",
                color = OceanBluePrimary,
                subtitle = "Revenue rata-rata",
                modifier = Modifier.weight(1f)
            )
            UnitEcoCard(
                title = "BIAYA HPP",
                amount = formatRupiah(unitEco.variableCostPerKg + unitEco.fixedCostAllocationPerKg) + "/kg",
                color = DangerRed,
                subtitle = "Var + Alokasi Tetap",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UnitEcoCard(
                title = "CONTRIBUTION",
                amount = formatRupiah(unitEco.contributionMarginPerKg) + "/kg",
                color = WarningYellow,
                subtitle = "Harga jual - Var Cost",
                modifier = Modifier.weight(1f)
            )
            UnitEcoCard(
                title = "NET PROFIT",
                amount = formatRupiah(unitEco.netProfitPerKg) + "/kg",
                color = SuccessGreen,
                subtitle = "Laba bersih per kg",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 10. BREAK EVEN POINT (BEP)
        Text(
            text = "TITIK IMPAS (BREAK EVEN POINT)",
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bep_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Large simple callout
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (bep.isProfitable) SuccessGreenBg else DangerRedBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Laundry harus memperoleh minimal ${bep.bepKgPerDay.toInt()} kg/hari agar mencapai BEP.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (bep.isProfitable) Color(0xFF065F46) else Color(0xFF991B1B)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (bep.gapKgPerDay >= 0) {
                                "Kondisi saat ini: Aman di atas BEP (Surplus +${bep.gapKgPerDay.toInt()} kg/hari)."
                            } else {
                                "Kondisi saat ini: Defisit ${bep.gapKgPerDay.toInt()} kg/hari di bawah titik impas!"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (bep.isProfitable) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Fixed Cost / Bulan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    Text(formatRupiah(bep.monthlyFixedCost), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Contribution Margin / kg", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    Text(formatRupiah(bep.contributionMarginPerKg), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BEP Volume Bulanan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    Text("${bep.bepKgPerMonth.toInt()} kg/bulan", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BEP Omzet Bulanan", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    Text(formatRupiah(bep.bepOmzetMonthly), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Volume Aktual Hari Ini", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    Text("${bep.actualKgPerDay.toInt()} kg/hari", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gap ke BEP", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    Text(
                        text = "${if (bep.gapKgPerDay >= 0) "+" else ""}${bep.gapKgPerDay.toInt()} kg",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (bep.gapKgPerDay >= 0) SuccessGreen else DangerRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 8. COST ENGINE (FIXED VS VARIABLE BREAKDOWN)
        Text(
            text = "STRUKTUR BIAYA (COST ENGINE)",
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
                Text(
                    text = "FIXED COST (BIAYA TETAP BULANAN)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate500)
                )
                Spacer(modifier = Modifier.height(6.dp))
                CostBreakdownRow("Sewa Tempat", formatRupiah(config.rentMonthly))
                CostBreakdownRow("Gaji Karyawan (${config.staffCount} orang)", formatRupiah(config.totalStaffCostMonthly))
                CostBreakdownRow("Internet & Software", formatRupiah(config.internetSoftwareMonthly))
                CostBreakdownRow("Depresiasi Mesin", formatRupiah(config.depreciationMonthly))
                CostBreakdownRow("Administrasi & Lainnya", formatRupiah(config.adminOtherMonthly))
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 4.dp))
                CostBreakdownRow("Total Fixed Cost", formatRupiah(config.totalPureFixedCostMonthly), isBold = true)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "VARIABLE COST (BIAYA VARIABEL / KG)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate500)
                )
                Spacer(modifier = Modifier.height(6.dp))
                CostBreakdownRow("Detergen, Softener & Parfum", "${formatRupiah(config.chemicalPerKg)}/kg")
                CostBreakdownRow("Plastik Packing", "${formatRupiah(config.plasticPerKg)}/kg")
                CostBreakdownRow("Gas Dryer & Lainnya", "${formatRupiah(config.gasAndOtherPerKg)}/kg")
                CostBreakdownRow("Listrik & Air (Rata-rata/kg)", "${formatRupiah((config.electricityMonthlyBase + config.waterMonthlyBase) / (config.targetKgPerDay * 30.0))}/kg")
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 4.dp))
                CostBreakdownRow("Total Variable Cost", "${formatRupiah(config.variableCostPerKg)}/kg", isBold = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 14. CASHFLOW FORECAST
        Text(
            text = "FORECAST CASHFLOW",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kas Awal", style = MaterialTheme.typography.bodyMedium.copy(color = Slate600))
                    Text(formatRupiah(config.initialCash), fontWeight = FontWeight.Bold, color = Slate900)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate100)
                Spacer(modifier = Modifier.height(10.dp))

                cashflowForecast.forEach { item ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.periodLabel, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate800))
                            Text(formatRupiahCompact(item.endingCashBalance), fontWeight = FontWeight.ExtraBold, color = OceanBluePrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Masuk: ${formatRupiahCompact(item.projectedRevenue)} | Keluar: ${formatRupiahCompact(item.projectedExpenses)}", style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp))
                            Text("Net: ${formatRupiahCompact(item.projectedNetCash)}", style = MaterialTheme.typography.bodySmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                        }
                    }
                    if (item != cashflowForecast.last()) {
                        HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 17. CUSTOMER ECONOMICS
        Text(
            text = "CUSTOMER ECONOMICS",
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
                CostBreakdownRow("Pelanggan Aktif", "${custEco.activeCustomers} orang")
                CostBreakdownRow("Rata-rata Transaksi", formatRupiah(custEco.avgTransactionRp))
                CostBreakdownRow("Rata-rata kg / Customer", "${custEco.avgKgPerCustomer} kg")
                CostBreakdownRow("Frekuensi Laundry", "${custEco.monthlyFrequency}x / bulan")
                CostBreakdownRow("Revenue / Customer / Bulan", formatRupiah(custEco.revenuePerCustomerMonthly))
                CostBreakdownRow("Repeat Customer Rate", "${custEco.repeatCustomerRate}%")
                CostBreakdownRow("Estimasi Lifetime Value (1 Thn)", formatRupiah(custEco.simpleCLV), isBold = true)
            }
        }
    }
}

@Composable
fun UnitEcoCard(
    title: String,
    amount: String,
    color: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun CostBreakdownRow(
    label: String,
    amount: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isBold) Slate900 else Slate600,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Slate900,
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium
            )
        )
    }
}
