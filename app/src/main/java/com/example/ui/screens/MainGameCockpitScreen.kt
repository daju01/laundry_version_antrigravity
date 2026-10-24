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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.cockpit.*
import com.example.ui.scene.Laundry3DScene
import com.example.ui.theme.*
import com.example.viewmodel.GameCockpitViewModel
import com.example.viewmodel.GameUiState

/**
 * Step-by-Step, Kid-Friendly Progressive Game & Operations Screen.
 * - Tab 0 (Ruang 3D): Shows the Compact HUD, Smart "Apa yang Harus Dilakukan Sekarang?" Guide,
 *   the Full Unobstructed 3D Isometric Laundry Studio, Streak Card, and Quick Quests.
 * - Tab 1 (Alur Cucian): Focused step-by-step view of all 6 Laundry Stations + Direct Boost/Manage buttons
 *   + 1-tap switch to Full Customer Order Queue (ERP Operasi).
 * - Tab 2 (Tim & Alat): Focused view of Staff, Machines, Shop, and Daily Quests (zero cut-off buttons).
 * - Tab 3 (Celengan Kas): Focused view of Today's Cash, Profit/Loss, Level Progress, and 1-tap links
 *   to KPI Dashboard, BEP Calculator, and What-If Business Simulator.
 */
@Composable
fun MainGameCockpitScreen(
    viewModel: GameCockpitViewModel,
    uiState: GameUiState,
    externalDetailTab: Int? = null,
    onDetailTabChanged: ((Int) -> Unit)? = null,
    onOpenErpMode: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeEngine = if (uiState.isLabModeActive && uiState.labEngine != null) uiState.labEngine else uiState.engine
    val bottleneck = remember(activeEngine.currentMinuteOfDay, activeEngine.ordersList.size, uiState.stateRevision) {
        activeEngine.diagnoseBottleneck()
    }

    var internalDetailTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    LaunchedEffect(externalDetailTab) {
        if (externalDetailTab != null && externalDetailTab != internalDetailTab) {
            internalDetailTab = externalDetailTab
            scrollState.scrollTo(0)
        }
    }
    val selectedDetailTab = externalDetailTab ?: internalDetailTab
    val selectTab: (Int) -> Unit = { idx ->
        internalDetailTab = idx
        onDetailTabChanged?.invoke(idx)
    }

    LaunchedEffect(selectedDetailTab) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppCanvasBg, AppCanvasSecondaryBg)
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp, bottom = 92.dp)
    ) {
        // STEP NAVIGATION PILLS (Clear 1-2-3-4 Progressive Guide)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DetailTabButton(
                label = "1. 🏠 3D",
                accentColor = OceanBluePrimary,
                isSelected = selectedDetailTab == 0,
                onClick = { selectTab(0) },
                modifier = Modifier.weight(1f)
            )
            DetailTabButton(
                label = "2. 🧺 Alur",
                accentColor = MintEmerald,
                isSelected = selectedDetailTab == 1,
                onClick = { selectTab(1) },
                modifier = Modifier.weight(1f)
            )
            DetailTabButton(
                label = "3. 👥 Tim",
                accentColor = TealAccent,
                isSelected = selectedDetailTab == 2,
                onClick = { selectTab(2) },
                modifier = Modifier.weight(1f)
            )
            DetailTabButton(
                label = "4. 💰 Kas",
                accentColor = BerryLavender,
                isSelected = selectedDetailTab == 3,
                onClick = { selectTab(3) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedDetailTab) {
            0 -> {
                // =========================================================================
                // STEP 1: RUANG 3D LAUNDRY (Fokus Bermain & Ketuk Alat 3D)
                // =========================================================================
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

                // SMART KID-FRIENDLY GUIDE BANNER ("Apa yang Harus Dilakukan Sekarang?")
                SmartNextStepBanner(
                    bottleneck = bottleneck,
                    onBoostPhase = { viewModel.focusAndBoostStation(bottleneck.phase) },
                    onInspectPhase = { viewModel.selectStationPhase(bottleneck.phase) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // UNOBSTRUCTED, REACTIVE 3D ISOMETRIC CUTAWAY DIORAMA SCENE
                Laundry3DScene(
                    engine = activeEngine,
                    selectedPhase = uiState.selectedPhase,
                    onSelectPhase = { viewModel.focusAndBoostStation(it) },
                    onInteractStation = { viewModel.focusAndBoostStation(it) },
                    onOpenInspector = { viewModel.selectStationPhase(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // COMPACT STREAK & SATISFACTION BANNER
                ZeroLateStreakCard(
                    engine = activeEngine,
                    onOpenDetails = { viewModel.openZeroLateStreakModal() },
                    onClaimSatisfactionBonus = { viewModel.claimVisualSatisfactionBonus() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // DAILY QUESTS & SIMULATION TRIGGERS
                QuestsTabContent(
                    engine = activeEngine,
                    onClaim = { viewModel.claimQuest(it) },
                    onTestLevelUp = { viewModel.triggerTestLevelUp() },
                    onTestStreak = { viewModel.triggerTestStreak() }
                )
            }

            1 -> {
                // =========================================================================
                // STEP 2: ALUR 6 STASIUN & ANTREAN ORDER (Bersih tanpa terpotong kartu 3D!)
                // =========================================================================
                StepGuideHeaderCard(
                    stepTitle = "Langkah 2: Urutan Proses Cucian (1 sampai 6)",
                    stepSubtitle = "Cucian masuk dari tahap 1 sampai 6. Ketuk tombol '⚡ Bantu' untuk mempercepat cucian, atau '🔧 Atur' untuk memindahkan pekerja!",
                    accentColor = MintEmerald,
                    actionLabel = "📋 Buka Daftar Order Lengkap",
                    onActionClick = { onOpenErpMode?.invoke("OPERASI") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SmartNextStepBanner(
                    bottleneck = bottleneck,
                    onBoostPhase = { viewModel.focusAndBoostStation(bottleneck.phase) },
                    onInspectPhase = { viewModel.selectStationPhase(bottleneck.phase) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PipelineTabContent(
                    engine = activeEngine,
                    onBoostPhase = { viewModel.focusAndBoostStation(it) },
                    onSelectPhase = { viewModel.selectStationPhase(it) }
                )
            }

            2 -> {
                // =========================================================================
                // STEP 3: TIM PEKERJA, MESIN & MISI (Bersih, tombol tidak terpotong!)
                // =========================================================================
                StepGuideHeaderCard(
                    stepTitle = "Langkah 3: Kelola Pekerja, Mesin & Misi Hadiah",
                    stepSubtitle = "Jaga energi Budi & Siti di atas 50% dengan Kopi, beli mesin baru di Toko saat uang cukup, dan klaim hadiah misi harianmu!",
                    accentColor = TealAccent,
                    actionLabel = "⚙️ Atur Tarif & Kapasitas Toko",
                    onActionClick = { onOpenErpMode?.invoke("SETTING") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TeamAndAssetsTabContent(
                    engine = activeEngine,
                    onOpenShop = { viewModel.openShop() },
                    onBoostCoffee = { viewModel.giveCoffeeBoost() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ZeroLateStreakCard(
                    engine = activeEngine,
                    onOpenDetails = { viewModel.openZeroLateStreakModal() },
                    onClaimSatisfactionBonus = { viewModel.claimVisualSatisfactionBonus() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                QuestsTabContent(
                    engine = activeEngine,
                    onClaim = { viewModel.claimQuest(it) },
                    onTestLevelUp = { viewModel.triggerTestLevelUp() },
                    onTestStreak = { viewModel.triggerTestStreak() }
                )
            }

            3 -> {
                // =========================================================================
                // STEP 4: CELENGAN KAS, LABA-RUGI & SIMULATOR BISNIS LENGKAP
                // =========================================================================
                StepGuideHeaderCard(
                    stepTitle = "Langkah 4: Celengan Uang & Kalkulator Bisnis",
                    stepSubtitle = "Lihat uang masuk hari ini, hitung kapan balik modal (BEP), atau coba eksperimen harga di Lab Simulator!",
                    accentColor = BerryLavender,
                    actionLabel = "🎛️ Buka Simulator Bisnis",
                    onActionClick = { onOpenErpMode?.invoke("SIMULATOR") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FinanceAndTierTabContent(
                    engine = activeEngine,
                    onOpenErpMode = onOpenErpMode
                )
            }
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
private fun SmartNextStepBanner(
    bottleneck: BottleneckDiagnosis,
    onBoostPhase: () -> Unit,
    onInspectPhase: () -> Unit
) {
    val isHeavyBottleneck = bottleneck.loadKg >= 12.0
    val bannerColor = if (isHeavyBottleneck) CoralRose else MintEmerald
    val bannerBg = if (isHeavyBottleneck) CoralTintBg else MintTintBg

    Card(
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(bannerColor)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectPhase() }
            .testTag("bottleneck_summary_card")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bannerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isHeavyBottleneck) "🚨" else "💡", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PETUNJUK SEKARANG: ${bottleneck.phase.icon} ${bottleneck.phase.title.uppercase()} (${bottleneck.loadKg.toInt()} kg)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHeavyBottleneck) CoralRoseDark else MintEmeraldDark,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bottleneck.cause,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate800,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bannerColor)
                    .clickable { onBoostPhase() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("⚡ Bantu", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun StepGuideHeaderCard(
    stepTitle: String,
    stepSubtitle: String,
    accentColor: Color,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.45f))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stepTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stepSubtitle,
                fontSize = 11.sp,
                color = Slate700,
                lineHeight = 15.sp
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.fillMaxWidth().height(34.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
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
            .shadow(if (isSelected) 3.dp else 0.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor else Color.White)
            .border(1.dp, if (isSelected) accentColor else CardBorderLight, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else Slate700,
            maxLines = 1
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
                text = "🎯 MISI HARIAN BERHADIAH (SELESAIKAN & KLAIM UANG!)",
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
                    Text("⭐ Coba Naik Level", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
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
                    Text("🔥 Coba Streak", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PipelineTabContent(
    engine: SimulationEngine,
    onBoostPhase: (StationPhase) -> Unit,
    onSelectPhase: (StationPhase) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "URUTAN 6 STASIUN LAUNDRY (KETUK UNTUK BANTU / ATUR):",
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
                val progress = if (phaseKg > 0) engine.getPhaseProgressFraction(phase) else 0f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(phaseColor.copy(alpha = 0.08f))
                        .border(1.dp, phaseColor.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                        .clickable { onSelectPhase(phase) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(phaseColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(phase.icon, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Tahap ${phase.step}: ${phase.title}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "$assignedCount pekerja • ${phaseOrders.size} tumpukan (${phaseKg.toInt()} kg)",
                                    fontSize = 10.5.sp,
                                    color = Slate600
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(phaseColor)
                                    .clickable { onBoostPhase(phase) }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "⚡ Bantu",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, phaseColor, RoundedCornerShape(8.dp))
                                    .clickable { onSelectPhase(phase) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "🔧 Atur",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = phaseColor
                                )
                            }
                        }
                    }

                    if (phaseKg > 0.0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = phaseColor,
                            trackColor = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamAndAssetsTabContent(
    engine: SimulationEngine,
    onOpenShop: () -> Unit,
    onBoostCoffee: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "👷 TIM OPERATOR LAUNDRY (${engine.workerList.size} ORANG)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TealAccent
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Clean two-column equal-width action row so button text is NEVER cut off!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBoostCoffee,
                    colors = ButtonDefaults.buttonColors(containerColor = SunnyAmber),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        text = "☕ Beri Kopi Staf",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = onOpenShop,
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        text = "➕ Rekrut & Beli Alat",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        Text("Tugas Sekarang: Tahap ${worker.assignedPhase.step} (${worker.assignedPhase.title})", fontSize = 10.5.sp, color = Slate600)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val energyColor = if (worker.energyPercent > 60) MintEmeraldDark else CoralRose
                        Text("Energi ${worker.energyPercent}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = energyColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { worker.energyPercent / 100f },
                            modifier = Modifier
                                .width(72.dp)
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
                text = "🫧 MESIN & ALAT TERPASANG (${engine.equipmentList.size} UNIT)",
                fontSize = 11.5.sp,
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
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${eq.type.icon} ${eq.type.displayName}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    Text(
                        text = if (eq.isRunning) "🫧 Mencuci (${eq.remainingMinutesInCycle}m)" else "✅ Siap Pakai",
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
private fun FinanceAndTierTabContent(
    engine: SimulationEngine,
    onOpenErpMode: ((String) -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🪙 CELENGAN & LABA-RUGI HARI INI",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BerryLavenderDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            FinanceRow("➕ Uang Masuk Hari Ini", formatMoney(engine.revenueCollectedToday), MintEmeraldDark)
            FinanceRow("➖ Biaya Sabun, Listrik & Booster", formatMoney(engine.operationalExpensesToday), CoralRose)
            val netEst = engine.revenueCollectedToday - engine.operationalExpensesToday
            FinanceRow("💰 Untung Bersih Hari Ini", formatMoney(netEst), if (netEst >= 0) MintEmeraldDark else CoralRose, isBold = true)
            FinanceRow("🏦 Total Uang Kas di Celengan", formatMoney(engine.cash), SunnyAmberDark, isBold = true)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🏆 LEVEL TOKO LAUNDRY KAMU",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OceanBlueDeep
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${engine.growthTier.title} (Level ${engine.tycoonLevel})", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selesaikan cucian tepat waktu untuk mengumpulkan bintang XP, memperbesar kapasitas toko, dan membuka mesin raksasa!",
                fontSize = 11.sp,
                color = Slate600,
                lineHeight = 16.sp
            )

            if (onOpenErpMode != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "📊 FITUR LENGKAP ANALISIS BISNIS (ERP):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onOpenErpMode("COCKPIT") },
                        colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("📊 Rapor KPI", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                    }
                    Button(
                        onClick = { onOpenErpMode("KEUANGAN") },
                        colors = ButtonDefaults.buttonColors(containerColor = SunnyAmberDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("📈 Hitung BEP", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onOpenErpMode("OPERASI") },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("🧺 Antrean Order", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                    }
                    Button(
                        onClick = { onOpenErpMode("SIMULATOR") },
                        colors = ButtonDefaults.buttonColors(containerColor = BerryLavender),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("🎛️ Simulator", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                    }
                }
            }
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
