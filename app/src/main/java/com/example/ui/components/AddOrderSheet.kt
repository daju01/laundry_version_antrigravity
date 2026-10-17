package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderBottomSheet(
    config: BusinessConfig,
    onDismiss: () -> Unit,
    onSubmit: (
        customerName: String,
        phone: String,
        serviceType: ServiceType,
        weightKg: Double,
        pricePerKg: Double,
        deadline: String,
        status: OrderStatus
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf(ServiceType.REGULER) }
    var weightKg by remember { mutableDoubleStateOf(5.5) }
    var selectedDeadline by remember { mutableStateOf("Hari ini") }
    var selectedStatus by remember { mutableStateOf(OrderStatus.MASUK) }

    val deadlines = listOf("Hari ini", "Besok", "2 hari", "3 hari")

    // Determine price per kg based on service and config
    val pricePerKg = when (selectedService) {
        ServiceType.REGULER -> config.regularPricePerKg
        ServiceType.EXPRESS -> config.expressPricePerKg
        ServiceType.CUCI_SETRIKA -> config.regularPricePerKg
        ServiceType.CUCI_SAJA -> config.regularPricePerKg * 0.67
        ServiceType.SETRIKA_SAJA -> config.regularPricePerKg * 0.67
        else -> selectedService.defaultPricePerKg
    }

    val totalPrice = weightKg * pricePerKg

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("add_order_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Input Order Cepat",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nama Pelanggan
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Nama Pelanggan") },
                placeholder = { Text("misal: Bu Ani / Pak Budi") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_customer_name"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Nomor HP (Optional)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP (Opsional)") },
                placeholder = { Text("08xxxxxxxxxx") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Jenis Layanan
            Text(
                text = "Jenis Layanan",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ServiceType.values()) { service ->
                    val isSelected = service == selectedService
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedService = service },
                        label = {
                            Text(
                                text = service.displayName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (service == ServiceType.EXPRESS) PurpleExpress else OceanBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stepper Berat Laundry [ - ] 5.5 kg [ + ]
            Text(
                text = "Berat Laundry",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Slate100),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            if (weightKg > 0.5) weightKg = (weightKg - 0.5).coerceAtLeast(0.5)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_minus_weight"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", tint = Slate900)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatKg(weightKg),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = OceanBlueDark,
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = "@ ${formatRupiah(pricePerKg)}/kg",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                    }

                    FilledIconButton(
                        onClick = { weightKg += 0.5 },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_plus_weight"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = OceanBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deadline
            Text(
                text = "Deadline Selesai",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                deadlines.forEach { d ->
                    val isSelected = d == selectedDeadline
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) OceanBluePrimary else Slate100)
                            .border(1.dp, if (isSelected) OceanBluePrimary else Slate200, RoundedCornerShape(10.dp))
                            .clickable { selectedDeadline = d }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = d,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate700
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Awal
            Text(
                text = "Status Awal",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(OrderStatus.values()) { st ->
                    val isSelected = st == selectedStatus
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatus = st },
                        label = { Text(st.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Total Card & Submit Button
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SuccessGreenBorder)),
                modifier = Modifier.fillMaxWidth()
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
                            text = "TOTAL BAYAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        )
                        Text(
                            text = formatRupiah(totalPrice),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D),
                                fontSize = 22.sp
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onSubmit(
                                customerName.ifBlank { "Pelanggan Baru" },
                                phone,
                                selectedService,
                                weightKg,
                                pricePerKg,
                                selectedDeadline,
                                selectedStatus
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_submit_order")
                    ) {
                        Text("Simpan Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
