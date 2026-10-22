package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.cockpit.*
import com.example.ui.scene.Laundry3DScene
import com.example.ui.theme.*
import com.example.viewmodel.GameCockpitViewModel
import com.example.viewmodel.GameUiState

@Composable
fun MainGameCockpitScreen(
    viewModel: GameCockpitViewModel,
    uiState: GameUiState,
    externalDetailTab: Int? = null,
    onDetailTabChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeEngine = if (uiState.isLabModeActive && uiState.labEngine != null) uiState.labEngine else uiState.engine
    val bottleneck = remember(activeEngine.currentMinuteOfDay, activeEngine.ordersList.size, uiState.stateRevision) {
        activeEngine.diagnoseBottleneck()
    }

    var internalDetailTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(externalDetailTab) {
        if (externalDetailTab != null) {
            internalDetailTab = externalDetailTab
        }
    }
    val selectedDetailTab = externalDetailTab ?: internalDetailTab
    val selectTab: (Int) -> Unit = { idx ->
        internalDetailTab = idx
        onDetailTabChanged?.invoke(idx)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppCanvasBg, AppCanvasSecondaryBg)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp, bottom = 92.dp)
    ) {
        // 1. CLEAN COLORFUL TYCOON HUD HEADER BAR
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

        Spacer(modifier = Modifier.height(10.dp))

        // 1.5. COMPACT STREAK & SATISFACTION BANNER
        ZeroLateStreakCard(
            engine = activeEngine,
            onOpenDetails = { viewModel.openZeroLateStreakModal() },
            onClaimSatisfactionBonus = { viewModel.claimVisualSatisfactionBonus() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. TRUE 3D ISOMETRIC CUTAWAY DIORAMA SCENE
        Laundry3DScene(
            engine = activeEngine,
            selectedPhase = uiState.selectedPhase,
            onSelectPhase = { viewModel.selectStationPhase(it) },
            onInteractStation = { viewModel.interactWithStation(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. CLEAN BOTTLENECK STATUS & ACTION CARD
        val isHeavyBottleneck = bottleneck.loadKg >= 12.0
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isHeavyBottleneck) CoralTintBg else MintTintBg
            ),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isHeavyBottleneck) CoralRose else MintEmerald
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.selectStationPhase(bottleneck.phase) }
                .testTag("bottleneck_summary_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isHeavyBottleneck) CoralRose else MintEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isHeavyBottleneck) "🚨" else "✅", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOKUS STASIUN: ${bottleneck.phase.title.uppercase()} (${bottleneck.loadKg.toInt()} kg)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isHeavyBottleneck) CoralRoseDark else MintEmeraldDark
                        )
                    )
                    Text(
                        text = bottleneck.cause,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isHeavyBottleneck) CoralRose else MintEmerald)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Kelola", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. COLORFUL SUB-PANEL TABS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailTabButton(
                label = "🎯 Misi",
                accentColor = SunnyAmberDark,
                isSelected = selectedDetailTab == 0,
                onClick = { selectTab(0) },
                modifier = Modifier.weight(1f)
            )
            DetailTabButton(
                label = "🧺 Alur",
                accentColor = OceanBluePrimary,
                isSelected = selectedDetailTab == 1,
                onClick = { selectTab(1) },
                modifier = Modifier.weight(1f)
            )
            DetailTabButton(
                label = "👥 Tim",
                accentColor = TealAccent,
                isSelected = selectedDetailTab == 2,
                onClick = { selectTab(2) },
                modifier = Modifier.weight(1f)
            )
            DetailTabButton(
                label = "📊 Kas",
                accentColor = BerryLavender,
                isSelected = selectedDetailTab == 3,
                onClick = { selectTab(3) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedDetailTab) {
            0 -> QuestsTabContent(
                engine = activeEngine,
                onClaim = { viewModel.claimQuest(it) },
                onTestLevelUp = { viewModel.triggerTestLevelUp() },
                onTestStreak = { viewModel.triggerTestStreak() }
            )
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
private fun DetailTabButton(
    label: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor else Color.White)
            .border(1.dp, if (isSelected) accentColor else CardBorderLight, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else Slate700
        )
    }
}

@Composable
private fun QuestsTabContent(
    engine: SimulationEngine,
    onClaim: (String) -> Unit,
    onTestLevelUp: () -> Unit,
    onTestStreak: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🎯 MISI HARIAN TYCOON",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SunnyAmberDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            engine.activeQuests.forEach { quest ->
                val progressFrac = (quest.currentProgress / quest.targetAmount).toFloat().coerceIn(0f, 1f)
                val isCompleted = quest.currentProgress >= quest.targetAmount

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) MintTintBg else Slate50
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isCompleted) MintTintBorder else Slate200)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(quest.title, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                                Text(quest.description, fontSize = 10.5.sp, color = Slate600)
                            }

                            if (quest.isClaimed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, MintEmerald, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Terklaim ✅", fontSize = 10.sp, color = MintEmeraldDark, fontWeight = FontWeight.Bold)
                                }
                            } else if (isCompleted) {
                                Button(
                                    onClick = { onClaim(quest.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Klaim 🎁", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            } else {
                                Text(
                                    "${quest.currentProgress.toInt()}/${quest.targetAmount.toInt()}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OceanBlueDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { progressFrac },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isCompleted) MintEmerald else SunnyAmber,
                                trackColor = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${formatMoneyCompact(quest.rewardMoney)} 🪙 +${quest.rewardXp}⭐",
                                fontSize = 10.sp,
                                color = SunnyAmberDark,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestLevelUp,
                    colors = ButtonDefaults.buttonColors(containerColor = SunnyAmber),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_trigger_test_level_up")
                ) {
                    Text("⭐ Simulasi Naik Level", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }

                Button(
                    onClick = onTestStreak,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRose),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_trigger_test_streak")
                ) {
                    Text("🔥 Simulasi Streak", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PipelineTabContent(engine: SimulationEngine, onSelectPhase: (StationPhase) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "6 STASIUN OPERASIONAL (KETUK UNTUK DETAIL):",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OceanBlueDeep
            )
            Spacer(modifier = Modifier.height(8.dp))

            StationPhase.values().forEach { phase ->
                val phaseOrders = engine.ordersList.filter { it.currentPhase == phase }
                val phaseKg = phaseOrders.sumOf { it.weightKg }
                val assignedCount = engine.workerList.count { it.assignedPhase == phase }
                val phaseColor = Color(phase.colorHex)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(phaseColor.copy(alpha = 0.08f))
                        .border(1.dp, phaseColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .clickable { onSelectPhase(phase) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(phaseColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(phase.icon, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("${phase.step}. ${phase.title}", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                            Text("$assignedCount staf ditugaskan • ${phaseOrders.size} batch", fontSize = 10.5.sp, color = Slate600)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, phaseColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${phaseKg.toInt()} kg", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = phaseColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamAndAssetsTabContent(engine: SimulationEngine, onOpenShop: () -> Unit, onBoostCoffee: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIM OPERATOR (${engine.workerList.size} ORANG)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealAccent
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onBoostCoffee,
                        colors = ButtonDefaults.buttonColors(containerColor = SunnyAmber),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("☕ Kopi Staf", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Button(
                        onClick = onOpenShop,
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+ Rekrut & Alat", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            engine.workerList.forEach { worker ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate50)
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${worker.name} ${worker.moodEmoji}", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                        Text("Stasiun: ${worker.assignedPhase.title}", fontSize = 10.5.sp, color = Slate600)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val energyColor = if (worker.energyPercent > 60) MintEmeraldDark else CoralRose
                        Text("Energi ${worker.energyPercent}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = energyColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { worker.energyPercent / 100f },
                            modifier = Modifier
                                .width(68.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = energyColor,
                            trackColor = Slate200
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MESIN & PERALATAN TERPASANG (${engine.equipmentList.size} UNIT)",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OceanBlueDeep
            )
            Spacer(modifier = Modifier.height(6.dp))

            engine.equipmentList.forEach { eq ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SkyTintBg.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${eq.type.icon} ${eq.type.displayName}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    Text(
                        text = if (eq.isRunning) "🫧 Aktif (${eq.remainingMinutesInCycle}m)" else "✅ Siap Pakai",
                        fontSize = 10.5.sp,
                        color = if (eq.isRunning) OceanBlueDark else MintEmeraldDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceAndTierTabContent(engine: SimulationEngine) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📊 RINGKASAN LABA-RUGI HARI INI",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BerryLavenderDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            FinanceRow("Pendapatan Masuk", formatMoney(engine.revenueCollectedToday), MintEmeraldDark)
            FinanceRow("Biaya Operasional", formatMoney(engine.operationalExpensesToday), CoralRose)
            val netEst = engine.revenueCollectedToday - engine.operationalExpensesToday
            FinanceRow("Estimasi Laba Bersih", formatMoney(netEst), if (netEst >= 0) MintEmeraldDark else CoralRose, isBold = true)
            FinanceRow("Saldo Kas Tersedia", formatMoney(engine.cash), SunnyAmberDark, isBold = true)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🏆 LEVEL EKSPANSI OUTLET",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OceanBlueDeep
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(engine.growthTier.title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selesaikan cucian tepat waktu untuk mengumpulkan XP, meningkatkan kapasitas harian, dan membuka mesin industri!",
                fontSize = 11.sp,
                color = Slate600,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun FinanceRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.5.sp, color = Slate600, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
        Text(value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold, color = color)
    }
}
