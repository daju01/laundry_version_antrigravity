package com.example.ui.cockpit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1E293B),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("station_inspector_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(phase.colorHex))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stasiun ${phase.step}: ${phase.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottleneck Alert Box
            if (isCurrentBottleneck) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TITIK HAMBATAN UTAMA (BOTTLENECK)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFFCA5A5))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bottleneck.cause,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFEE2E2), lineHeight = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 Saran Solusi: ${bottleneck.suggestion}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFEF08A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Metric Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Beban Antrean", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text("${phaseKg.toInt()} kg", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("${phaseOrders.size} order aktif", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Pekerja Ditugaskan", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text("${assignedWorkers.size} Orang", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                        Text(if (assignedWorkers.isEmpty()) "⚠️ Kosong!" else assignedWorkers.joinToString { it.name.substringBefore(" ") }, fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action 1: Reassign Worker to this Station
            Text(
                text = "TUGASKAN PEKERJA KE STASIUN INI",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (otherWorkers.isEmpty()) {
                Text(
                    text = "Semua pekerja sudah ditugaskan di sini.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    otherWorkers.forEach { worker ->
                        OutlinedButton(
                            onClick = { onReassignWorker(worker.id, phase) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF38BDF8))),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("Alihkan ${worker.name.substringBefore(" ")}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        containerColor = if (engine.isOvertimeActiveToday) Color(0xFFEF4444) else Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(
                        if (engine.isOvertimeActiveToday) "Batalkan Lembur" else "Lembur (+2 Jam)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onOpenShop,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("Beli Alat Tambahan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active orders in this phase
            Text(
                text = "DAFTAR ORDER MENUNGGU (${phaseOrders.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (phaseOrders.isEmpty()) {
                Text("Tidak ada cucian di stasiun ini.", fontSize = 11.sp, color = Color(0xFF64748B))
            } else {
                phaseOrders.take(4).forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(order.customerName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${order.id} • ${if (order.isExpress) "⚡ Express" else "Reguler"}", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        }
                        Text("${order.weightKg} kg", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}
