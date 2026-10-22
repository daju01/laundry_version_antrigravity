package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationInspectorModal(
    phase: StationPhase,
    engine: SimulationEngine,
    onDismiss: () -> Unit,
    onReassignWorker: (workerId: String, newPhase: StationPhase) -> Unit,
    onToggleOvertime: () -> Unit,
    onOpenShop: () -> Unit
) {
    val phaseOrders = engine.ordersList.filter { it.currentPhase == phase }
    val phaseKg = phaseOrders.sumOf { it.weightKg }
    val assignedWorkers = engine.workerList.filter { it.assignedPhase == phase }
    val otherWorkers = engine.workerList.filter { it.assignedPhase != phase }
    val bottleneck = engine.diagnoseBottleneck()
    val isCurrentBottleneck = bottleneck.phase == phase
    val phaseColor = Color(phase.colorHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("station_inspector_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Station Pill & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(phaseColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(phase.icon, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STASIUN ${phase.step} • OPERASIONAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = phaseColor
                        )
                        Text(
                            text = "Stasiun ${phase.title}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottleneck Alert Box
            if (isCurrentBottleneck) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CoralTintBg),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoralRose)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CoralRose, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TITIK HAMBATAN UTAMA (BOTTLENECK)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = CoralRoseDark)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bottleneck.cause,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate800, lineHeight = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 Solusi: ${bottleneck.suggestion}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CoralRoseDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Metric Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SkyTintBg),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkyTintBorder)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Beban Antrean", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OceanBlueDeep)
                        Text("${phaseKg.toInt()} kg", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        Text("${phaseOrders.size} order aktif", fontSize = 10.5.sp, color = Slate600)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MintTintBg),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MintTintBorder)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pekerja Aktif", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintEmeraldDark)
                        Text("${assignedWorkers.size} Orang", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        Text(
                            text = if (assignedWorkers.isEmpty()) "⚠️ Belum ada staf" else assignedWorkers.joinToString { it.name.substringBefore(" ") },
                            fontSize = 10.5.sp,
                            color = Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action 1: Reassign Worker to this Station
            Text(
                text = "TUGASKAN PEKERJA KE STASIUN INI",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Slate600)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (otherWorkers.isEmpty()) {
                Text(
                    text = "Semua pekerja sudah bertugas di stasiun ini.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    otherWorkers.forEach { worker ->
                        OutlinedButton(
                            onClick = { onReassignWorker(worker.id, phase) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OceanBlueDeep),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OceanBluePrimary)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("Alihkan ${worker.name.substringBefore(" ")}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action 2 & 3: Quick Overtime & Buy Machine Shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleOvertime,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (engine.isOvertimeActiveToday) CoralRose else SunnyAmber
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        if (engine.isOvertimeActiveToday) "Batalkan Lembur" else "⏰ Lembur (+2 Jam)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onOpenShop,
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("🏪 Beli Mesin/Alat", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active orders in this phase
            Text(
                text = "DAFTAR ORDER DI STASIUN (${phaseOrders.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Slate600)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (phaseOrders.isEmpty()) {
                Text("Tidak ada cucian yang mengantre di stasiun ini.", fontSize = 11.5.sp, color = Slate500)
            } else {
                phaseOrders.take(4).forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate50)
                            .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(order.customerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("${order.id} • ${if (order.isExpress) "⚡ Express" else "Reguler"}", fontSize = 10.sp, color = Slate500)
                        }
                        Text("${order.weightKg} kg", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = OceanBlueDark)
                    }
                }
            }
        }
    }
}
