package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.*
import com.example.viewmodel.CockpitUiState

@Composable
fun OperasiScreen(
    state: CockpitUiState,
    onAdvanceOrderStatus: (String) -> Unit,
    onDeleteOrder: (String) -> Unit,
    onOpenAddOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pipeline = remember(state) {
        BusinessEngine.computePipeline(state)
    }
    val config = state.config

    var selectedFilterStatus by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = remember(state.orders, selectedFilterStatus) {
        if (selectedFilterStatus == null) {
            state.orders.filter { it.status != OrderStatus.DIAMBIL }
        } else {
            state.orders.filter { it.status == selectedFilterStatus }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OPERASIONAL & PIPELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBluePrimary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Alur Proses & Kapasitas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                    )
                }

                Button(
                    onClick = onOpenAddOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_operasi_add_order")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Order", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Bottleneck Alert Banner (Auto-detected!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DangerRedBg),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DangerRedBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottleneck_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DETEKSI BOTTLENECK SAAT INI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DangerRed,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "Tahap Kritis: ${pipeline.bottleneck.stage}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = pipeline.bottleneck.reason,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700,
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kapasitas: ${pipeline.bottleneck.capacityKg.toInt()} kg/hari",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                        Text(
                            text = "Beban: ${pipeline.bottleneck.currentLoadKg.toInt()} kg",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate900, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Utilisasi: ${pipeline.bottleneck.utilizationPercent.toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(color = DangerRed, fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TipsAndUpdates, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pipeline.bottleneck.recommendation,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // VISUAL PIPELINE (MASUK -> CUCI -> KERING -> SETRIKA -> PACKING -> SELESAI)
        item {
            Text(
                text = "ALUR PROSES LAUNDRY (PIPELINE)",
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
                    PipelineStageRow(
                        step = "1",
                        name = "Masuk",
                        kg = pipeline.masukKg,
                        badgeColor = SuccessGreen,
                        subText = "Antrian awal timbangan",
                        isSelected = selectedFilterStatus == OrderStatus.MASUK,
                        onClick = {
                            selectedFilterStatus = if (selectedFilterStatus == OrderStatus.MASUK) null else OrderStatus.MASUK
                        }
                    )
                    PipelineConnector()
                    PipelineStageRow(
                        step = "2",
                        name = "Cuci",
                        kg = pipeline.cuciKg,
                        badgeColor = OceanBluePrimary,
                        subText = "${config.washingMachineCount} mesin cuci aktif",
                        isSelected = selectedFilterStatus == OrderStatus.CUCI,
                        onClick = {
                            selectedFilterStatus = if (selectedFilterStatus == OrderStatus.CUCI) null else OrderStatus.CUCI
                        }
                    )
                    PipelineConnector()
                    PipelineStageRow(
                        step = "3",
                        name = "Kering",
                        kg = pipeline.keringKg,
                        badgeColor = Color(0xFF7C3AED),
                        subText = "${config.dryerCount} dryer gas aktif",
                        isSelected = selectedFilterStatus == OrderStatus.KERING,
                        onClick = {
                            selectedFilterStatus = if (selectedFilterStatus == OrderStatus.KERING) null else OrderStatus.KERING
                        }
                    )
                    PipelineConnector()
                    PipelineStageRow(
                        step = "4",
                        name = "Setrika",
                        kg = pipeline.setrikaKg,
                        badgeColor = DangerRed,
                        subText = "⚠️ Beban tinggi (${pipeline.bottleneck.utilizationPercent.toInt()}%)",
                        isSelected = selectedFilterStatus == OrderStatus.SETRIKA,
                        onClick = {
                            selectedFilterStatus = if (selectedFilterStatus == OrderStatus.SETRIKA) null else OrderStatus.SETRIKA
                        }
                    )
                    PipelineConnector()
                    PipelineStageRow(
                        step = "5",
                        name = "Packing",
                        kg = pipeline.packingKg,
                        badgeColor = Color(0xFFDB2777),
                        subText = "Lipat, wangi & plastik rapi",
                        isSelected = selectedFilterStatus == OrderStatus.PACKING,
                        onClick = {
                            selectedFilterStatus = if (selectedFilterStatus == OrderStatus.PACKING) null else OrderStatus.PACKING
                        }
                    )
                    PipelineConnector()
                    PipelineStageRow(
                        step = "6",
                        name = "Selesai",
                        kg = pipeline.selesaiKg,
                        badgeColor = OceanBlueDark,
                        subText = "Siap diambil / diantar",
                        isSelected = selectedFilterStatus == OrderStatus.SELESAI,
                        onClick = {
                            selectedFilterStatus = if (selectedFilterStatus == OrderStatus.SELESAI) null else OrderStatus.SELESAI
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // MESIN & KAPASITAS AUDIT CARD
        item {
            Text(
                text = "KAPASITAS MESIN & EFISIENSI",
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
                    CapacityRow(
                        icon = Icons.Default.LocalLaundryService,
                        title = "Mesin Cuci (${config.washingMachineCount} unit @ ${config.washingCapacityPerMachineKg.toInt()}kg)",
                        teoritis = "${config.washingTheoreticalCapacityKg.toInt()} kg",
                        praktis = "${config.washingPracticalCapacityKg.toInt()} kg",
                        detail = "Cycle: ${config.washingCycleDurationMinutes.toInt()} mnt | Efisiensi: ${(config.operatingEfficiency * 100).toInt()}%"
                    )

                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 8.dp))

                    CapacityRow(
                        icon = Icons.Default.WindPower,
                        title = "Dryer / Pengering (${config.dryerCount} unit @ ${config.dryerCapacityPerMachineKg.toInt()}kg)",
                        teoritis = "${config.dryerTheoreticalCapacityKg.toInt()} kg",
                        praktis = "${config.dryerPracticalCapacityKg.toInt()} kg",
                        detail = "Cycle: ${config.dryerCycleDurationMinutes.toInt()} mnt | Efisiensi: ${(config.operatingEfficiency * 100).toInt()}%"
                    )

                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 8.dp))

                    CapacityRow(
                        icon = Icons.Default.Iron,
                        title = "Meja Setrika (${config.ironCount} unit)",
                        teoritis = "${config.ironTheoreticalCapacityKg.toInt()} kg",
                        praktis = "${config.ironPracticalCapacityKg.toInt()} kg",
                        detail = "Rate: ${config.ironCapacityPerUnitPerHourKg.toInt()} kg/jam | Jam kerja: ${config.operatingHoursPerDay.toInt()} jam"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // STAFF & PRODUKTIVITAS CARD
        item {
            Text(
                text = "STAFF & KARYAWAN",
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
                    val actualDailyKg = state.orders.filter { it.status != OrderStatus.DIAMBIL }.sumOf { it.weightKg }
                    val kgPerStaff = if (config.staffCount > 0) actualDailyKg / config.staffCount else 0.0
                    val omzetPerStaff = if (config.staffCount > 0) (actualDailyKg * config.regularPricePerKg) / config.staffCount else 0.0
                    val laborCostPerKg = if (actualDailyKg > 0) (config.totalStaffCostMonthly / 30.0) / actualDailyKg else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StaffMetricItem(label = "Total Karyawan", value = "${config.staffCount} orang")
                        StaffMetricItem(label = "Produktivitas", value = "${kgPerStaff.toInt()} kg/orang")
                        StaffMetricItem(label = "Biaya Tenaga/kg", value = formatRupiah(laborCostPerKg))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Staff Insight Indicator
                    val staffInsight = if (pipeline.setrikaKg > 30 && config.staffCount <= 4) {
                        "Kapasitas mesin masih cukup tetapi kapasitas setrika kekurangan ±1 orang."
                    } else {
                        "Jumlah tenaga kerja seimbang dengan beban cucian harian saat ini."
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = OceanBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = staffInsight,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // DAFTAR ORDER DI PIPELINE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedFilterStatus != null) "ORDER: ${selectedFilterStatus?.label?.uppercase()}" else "SEMUA ORDER DALAM PROSES (${filteredOrders.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        letterSpacing = 0.5.sp
                    )
                )

                if (selectedFilterStatus != null) {
                    TextButton(onClick = { selectedFilterStatus = null }) {
                        Text("Reset Filter", fontSize = 12.sp, color = OceanBluePrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada order pada tahap ini.", style = MaterialTheme.typography.bodyMedium.copy(color = Slate500))
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                OrderCardItem(
                    order = order,
                    onAdvance = { onAdvanceOrderStatus(order.id) },
                    onDelete = { onDeleteOrder(order.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PipelineStageRow(
    step: String,
    name: String,
    kg: Double,
    badgeColor: Color,
    subText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE0F2FE) else Slate50),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) OceanBluePrimary else Slate200)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${kg.toInt()} kg",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                )
            }
        }
    }
}

@Composable
fun PipelineConnector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun CapacityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    teoritis: String,
    praktis: String,
    detail: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = OceanBluePrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Kapasitas Teoritis: $teoritis",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
            )
            Text(
                text = "Praktis (80%): $praktis/hari",
                style = MaterialTheme.typography.bodySmall.copy(color = OceanBlueDark, fontWeight = FontWeight.Bold)
            )
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
        )
    }
}

@Composable
fun StaffMetricItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
    }
}

@Composable
fun OrderCardItem(
    order: LaundryOrder,
    onAdvance: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        if (order.serviceType == ServiceType.EXPRESS) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PurpleExpress)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "EXPRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "${order.id} • ${order.serviceType.displayName} • DL: ${order.deadline}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatKg(order.weightKg),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlueDark
                        )
                    )
                    Text(
                        text = formatRupiah(order.totalPrice),
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Progress Bar & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate100)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Status: ${order.status.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    )
                }

                // Next Step Button
                if (order.status.nextStatus() != null) {
                    Button(
                        onClick = onAdvance,
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Lanjut: ${order.status.nextStatus()?.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreenBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Lunas / Selesai",
                            style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
