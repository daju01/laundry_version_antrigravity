package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.cockpit.*
import com.example.ui.scene.Laundry3DScene
import com.example.viewmodel.GameCockpitViewModel
import com.example.viewmodel.GameUiState

@Composable
fun MainGameCockpitScreen(
    viewModel: GameCockpitViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val activeEngine = if (uiState.isLabModeActive && uiState.labEngine != null) uiState.labEngine else uiState.engine
    val bottleneck = remember(activeEngine.currentMinuteOfDay, activeEngine.ordersList.size) {
        activeEngine.diagnoseBottleneck()
    }

    var selectedDetailTab by remember { mutableIntStateOf(0) } // 0: Misi & Quest, 1: Alur Operasi, 2: Tim & Alat, 3: Keuangan

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp, bottom = 86.dp)
    ) {
        // 1. TYCOON HUD HEADER BAR (Level, XP, Coins, Speed, Boosters)
        CockpitHeaderBar(
            engine = activeEngine,
            onSpeedChanged = { viewModel.setSpeed(it) },
            onOpenLab = {
                if (uiState.isLabModeActive) {
                    viewModel.exitLabMode()
                } else {
                    viewModel.openLabModal()
                }
            },
            onOpenShop = { viewModel.openShop() },
            onManualCloseDay = { viewModel.manualCloseDay() },
            onCoffeeBoost = { viewModel.giveCoffeeBoost() },
            onTurboWash = { viewModel.activateTurboWash() },
            onPromoFlyer = { viewModel.triggerPromoFlyer() },
            onTestLevelUp = { viewModel.triggerTestLevelUp() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1.5. INDIKATOR STREAK EFISIENSI (BEBAS PESANAN TELAT & BONUS KEPUASAN)
        ZeroLateStreakCard(
            engine = activeEngine,
            onOpenDetails = { viewModel.openZeroLateStreakModal() },
            onClaimSatisfactionBonus = { viewModel.claimVisualSatisfactionBonus() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. HERO 3D ISOMETRIC LAUNDRY TYCOON SCENE
        Laundry3DScene(
            engine = activeEngine,
            selectedPhase = uiState.selectedPhase,
            onSelectPhase = { viewModel.selectStationPhase(it) },
            onInteractStation = { viewModel.interactWithStation(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. BOTTLENECK QUICK ALERT (GAMIFIED)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.selectStationPhase(bottleneck.phase) }
                .testTag("bottleneck_summary_card")
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x33EF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚨", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HAMBATAN KRITIS: ${bottleneck.phase.title.uppercase()} (${bottleneck.loadKg.toInt()} kg)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFFCA5A5))
                    )
                    Text(
                        text = bottleneck.cause,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 11.sp),
                        maxLines = 2
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Atasi", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. SUB-PANEL TABS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DetailTabButton("🎯 Misi", isSelected = selectedDetailTab == 0, onClick = { selectedDetailTab = 0 }, modifier = Modifier.weight(1f))
            DetailTabButton("🧺 Alur", isSelected = selectedDetailTab == 1, onClick = { selectedDetailTab = 1 }, modifier = Modifier.weight(1f))
            DetailTabButton("👥 Tim", isSelected = selectedDetailTab == 2, onClick = { selectedDetailTab = 2 }, modifier = Modifier.weight(1f))
            DetailTabButton("📊 Kas", isSelected = selectedDetailTab == 3, onClick = { selectedDetailTab = 3 }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        when (selectedDetailTab) {
            0 -> QuestsTabContent(activeEngine, onClaim = { viewModel.claimQuest(it) })
            1 -> PipelineTabContent(activeEngine, onSelectPhase = { viewModel.selectStationPhase(it) })
            2 -> TeamAndAssetsTabContent(activeEngine, onOpenShop = { viewModel.openShop() }, onBoostCoffee = { viewModel.giveCoffeeBoost() })
            3 -> FinanceAndTierTabContent(activeEngine)
        }
    }

    // MODALS & DIALOGS
    if (uiState.isStationInspectorOpen && uiState.selectedPhase != null) {
        StationInspectorModal(
            phase = uiState.selectedPhase,
            engine = activeEngine,
            onDismiss = { viewModel.closeStationInspector() },
            onReassignWorker = { workerId, newPhase -> viewModel.reassignWorker(workerId, newPhase) },
            onToggleOvertime = { viewModel.toggleOvertime() },
            onOpenShop = { viewModel.openShop() }
        )
    }

    if (uiState.isMorningPlanningOpen) {
        MorningPlanningDialog(
            engine = activeEngine,
            onStartDay = { quota, overtime -> viewModel.confirmMorningPlan(quota, overtime) }
        )
    }

    if (uiState.isEveningSummaryOpen && uiState.latestDayReport != null) {
        EveningSummaryDialog(
            report = uiState.latestDayReport,
            onContinueNextDay = { viewModel.continueToNextDay() }
        )
    }

    if (uiState.isShopOpen) {
        EquipmentShopModal(
            engine = activeEngine,
            onBuyEquipment = { viewModel.buyEquipment(it) },
            onHireStaff = { name, phase -> viewModel.hireWorker(name, phase) },
            onDismiss = { viewModel.closeShop() }
        )
    }

    if (uiState.isLabModalOpen) {
        LabExperimentModal(
            engine = activeEngine,
            onApplyDecisionToActive = { dryer, staff, overtime ->
                viewModel.applyLabDecisionToActive(dryer, staff, overtime)
            },
            onDismiss = { viewModel.closeLabModal() }
        )
    }

    if (uiState.isOnboardingOpen) {
        OnboardingTutorialDialog(
            onDismiss = { viewModel.closeOnboarding() }
        )
    }

    // CELEBRATION DIALOGS: LEVEL UP (HIGHER CAPACITY) & STREAKS
    if (activeEngine.pendingLevelUp != null) {
        LevelUpCelebrationDialog(
            celebration = activeEngine.pendingLevelUp!!,
            onClaim = { viewModel.claimLevelUp() }
        )
    }

    if (activeEngine.pendingStreakCelebration != null) {
        StreakCelebrationDialog(
            streak = activeEngine.pendingStreakCelebration!!,
            onDismiss = { viewModel.dismissStreakCelebration() }
        )
    }

    if (uiState.isZeroLateStreakModalOpen) {
        ZeroLateStreakDialog(
            engine = activeEngine,
            onDismiss = { viewModel.closeZeroLateStreakModal() },
            onCelebrate = { viewModel.claimVisualSatisfactionBonus() }
        )
    }
}

@Composable
private fun DetailTabButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))) else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B))))
            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}

// -------------------------------------------------------------
// TAB 0: MISI & DAILY QUESTS (GAMIFIED)
// -------------------------------------------------------------
@Composable
private fun QuestsTabContent(engine: SimulationEngine, onClaim: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("🎯 MISI HARIAN TYCOON (SELESAIKAN UNTUK KLAIM BONUS)", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24))
            Spacer(modifier = Modifier.height(8.dp))

            engine.activeQuests.forEach { quest ->
                val progressFrac = (quest.currentProgress / quest.targetAmount).toFloat().coerceIn(0f, 1f)
                val isCompleted = quest.currentProgress >= quest.targetAmount

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(quest.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(quest.description, fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }

                            if (quest.isClaimed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF334155))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Sudah Diklaim ✅", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            } else if (isCompleted) {
                                Button(
                                    onClick = { onClaim(quest.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Klaim Hadiah 🎁", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            } else {
                                Text(
                                    "${quest.currentProgress.toInt()}/${quest.targetAmount.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { progressFrac },
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (isCompleted) Color(0xFF10B981) else Color(0xFFFBBF24),
                                trackColor = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hadiah: +${formatMoneyCompact(quest.rewardMoney)} 🪙 +${quest.rewardXp}⭐", fontSize = 9.sp, color = Color(0xFFFDE68A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Experience Simulator Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { engine.triggerTestLevelUp() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("btn_trigger_test_level_up")
                ) {
                    Text("⭐ Test Level Up (+150 XP)", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                }

                Button(
                    onClick = {
                        engine.currentStreak++
                        engine.pendingStreakCelebration = StreakCelebration(
                            streakDays = engine.currentStreak,
                            bonusCash = engine.currentStreak * 75_000.0,
                            bonusXp = engine.currentStreak * 30,
                            title = "🔥 HARI EFISIEN BERTURUT-TURUT!",
                            multiplierPercent = (engine.currentStreak * 10).coerceAtMost(50)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("btn_trigger_test_streak")
                ) {
                    Text("🔥 Test Streak (+1 Hari)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: PIPELINE ALUR KERJA
// -------------------------------------------------------------
@Composable
private fun PipelineTabContent(engine: SimulationEngine, onSelectPhase: (StationPhase) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("6 FASE OPERASI (KETUK UNTUK UPGRADE / KELOLA):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))

            StationPhase.values().forEach { phase ->
                val phaseOrders = engine.ordersList.filter { it.currentPhase == phase }
                val phaseKg = phaseOrders.sumOf { it.weightKg }
                val assignedCount = engine.workerList.count { it.assignedPhase == phase }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .clickable { onSelectPhase(phase) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(phase.icon, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${phase.step}. ${phase.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("$assignedCount staf aktif", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("${phaseKg.toInt()} kg", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: TIM & ALAT
// -------------------------------------------------------------
@Composable
private fun TeamAndAssetsTabContent(engine: SimulationEngine, onOpenShop: () -> Unit, onBoostCoffee: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PEKERJA & KONDISI ENERGI (${engine.workerList.size}):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onBoostCoffee,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("☕ Traktir Kopi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onOpenShop, contentPadding = PaddingValues(0.dp)) {
                        Text("+ Rekrut", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    }
                }
            }

            engine.workerList.forEach { worker ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${worker.name} ${worker.moodEmoji}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Stasiun: ${worker.assignedPhase.title}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Energi: ${worker.energyPercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (worker.energyPercent > 60) Color(0xFF10B981) else Color(0xFFEF4444))
                        LinearProgressIndicator(
                            progress = { worker.energyPercent / 100f },
                            modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = if (worker.energyPercent > 60) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))

            Text("PERALATAN TERPASANG (${engine.equipmentList.size}):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(6.dp))

            engine.equipmentList.forEach { eq ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${eq.type.icon} ${eq.type.displayName}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Text(if (eq.isRunning) "🫧 Memproses" else "Siap", fontSize = 10.sp, color = if (eq.isRunning) Color(0xFF38BDF8) else Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: KEUANGAN & TIER STATS
// -------------------------------------------------------------
@Composable
private fun FinanceAndTierTabContent(engine: SimulationEngine) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("RINGKASAN LABA-RUGI HARI INI:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(6.dp))

            FinanceRow("Pendapatan Masuk", formatMoney(engine.revenueCollectedToday), Color(0xFF10B981))
            FinanceRow("Biaya Operasional", formatMoney(engine.operationalExpensesToday), Color(0xFFEF4444))
            val netEst = engine.revenueCollectedToday - engine.operationalExpensesToday
            FinanceRow("Estimasi Laba Bersih", formatMoney(netEst), if (netEst >= 0) Color(0xFF10B981) else Color(0xFFEF4444), isBold = true)
            FinanceRow("Saldo Kas Tersedia", formatMoney(engine.cash), Color(0xFFFBBF24), isBold = true)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))

            Text("PROGRESS TIER USAHA:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Text(engine.growthTier.title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Kumpulkan XP dari setiap pesanan dan pertahankan reputasi 5.0★ untuk ekspansi dari rumahan ke outlet ruko!",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun FinanceRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 11.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium, color = color)
    }
}
