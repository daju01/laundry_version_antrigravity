package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddOrderBottomSheet
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.GameCockpitViewModel
import com.example.viewmodel.LaundryCockpitViewModel

enum class NavGameTab(val title: String, val icon: ImageVector, val tag: String, val accentColor: Color) {
    COCKPIT_3D("1. Ruang 3D", Icons.Default.ViewInAr, "tab_3d_cockpit", OceanBluePrimary),
    PIPELINE("2. Alur", Icons.Default.LinearScale, "tab_pipeline", MintEmerald),
    TIM_ALAT("3. Tim & Alat", Icons.Default.Engineering, "tab_tim_alat", TealAccent),
    KEUANGAN("4. Kas & Lab", Icons.Default.AccountBalanceWallet, "tab_keuangan", BerryLavender),
    PANDUAN("5. Panduan", Icons.Default.HelpOutline, "tab_panduan", SunnyAmberDark)
}

enum class WorkspaceMode(val label: String, val tag: String, val primaryColor: Color, val secondaryColor: Color) {
    TYCOON_3D("🎮 Ruang 3D", "mode_tycoon_3d", OceanBluePrimary, CyanGlow),
    ERP_COCKPIT("📊 Rapor KPI", "mode_erp_cockpit", MintEmerald, TealAccent),
    ERP_OPERASI("🧺 Daftar Order", "mode_erp_operasi", OceanBlueDark, OceanBluePrimary),
    ERP_KEUANGAN("💰 Hitung BEP", "mode_erp_keuangan", SunnyAmberDark, SunnyAmber),
    ERP_SIMULATOR("🎛️ Simulator", "mode_erp_simulator", BerryLavender, Color(0xFFA855F7)),
    ERP_SETTING("⚙️ Tarif & Alat", "mode_erp_setting", CoralRose, Color(0xFFFB7185))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.audio.GameSoundManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                UnifiedLaundryGameApp()
            }
        }
    }
}

@Composable
fun UnifiedLaundryGameApp(
    viewModel: GameCockpitViewModel = viewModel(),
    erpViewModel: LaundryCockpitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val erpState by erpViewModel.uiState.collectAsStateWithLifecycle()

    var currentNavTab by remember { mutableStateOf(NavGameTab.COCKPIT_3D) }
    var currentWorkspaceMode by remember { mutableStateOf(WorkspaceMode.TYCOON_3D) }
    var tycoonDetailTab by remember { mutableIntStateOf(0) }

    BackHandler(enabled = currentNavTab != NavGameTab.COCKPIT_3D || currentWorkspaceMode != WorkspaceMode.TYCOON_3D) {
        currentNavTab = NavGameTab.COCKPIT_3D
        currentWorkspaceMode = WorkspaceMode.TYCOON_3D
        tycoonDetailTab = 0
    }

    val syncAndSwitchMode: (WorkspaceMode) -> Unit = { mode ->
        if (mode != WorkspaceMode.TYCOON_3D) {
            erpViewModel.syncFromSimulation(
                simConfig = viewModel.toBusinessConfig(),
                simOrders = viewModel.toBusinessOrders()
            )
        }
        currentWorkspaceMode = mode
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppCanvasBg,
        topBar = {
            WorkspaceModeSwitcherBar(
                currentMode = currentWorkspaceMode,
                onSelectMode = { mode ->
                    syncAndSwitchMode(mode)
                    if (mode == WorkspaceMode.TYCOON_3D) {
                        currentNavTab = NavGameTab.COCKPIT_3D
                        tycoonDetailTab = 0
                    }
                },
                onSyncGameToErp = {
                    erpViewModel.syncFromSimulation(
                        simConfig = viewModel.toBusinessConfig(),
                        simOrders = viewModel.toBusinessOrders()
                    )
                    if (currentWorkspaceMode == WorkspaceMode.TYCOON_3D) {
                        currentWorkspaceMode = WorkspaceMode.ERP_SIMULATOR
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 10.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .shadow(12.dp, spotColor = Color(0x220284C7))
                    .border(width = 1.dp, color = CardBorderLight)
                    .testTag("bottom_nav_bar")
            ) {
                NavGameTab.values().forEach { tab ->
                    val isSelected = currentNavTab == tab && currentWorkspaceMode == WorkspaceMode.TYCOON_3D
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab == NavGameTab.PANDUAN) {
                                viewModel.openOnboarding()
                            } else {
                                currentNavTab = tab
                                currentWorkspaceMode = WorkspaceMode.TYCOON_3D
                                viewModel.setBottomTab(tab.name)
                                tycoonDetailTab = when (tab) {
                                    NavGameTab.COCKPIT_3D -> 0
                                    NavGameTab.PIPELINE -> 1
                                    NavGameTab.TIM_ALAT -> 2
                                    NavGameTab.KEUANGAN -> 3
                                    NavGameTab.PANDUAN -> tycoonDetailTab
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = tab.accentColor,
                            selectedTextColor = tab.accentColor,
                            indicatorColor = tab.accentColor.copy(alpha = 0.14f),
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(AppCanvasBg, AppCanvasSecondaryBg)
                    )
                )
        ) {
            when (currentWorkspaceMode) {
                WorkspaceMode.TYCOON_3D -> {
                    MainGameCockpitScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        externalDetailTab = tycoonDetailTab,
                        onDetailTabChanged = { idx ->
                            tycoonDetailTab = idx
                            currentNavTab = when (idx) {
                                1 -> NavGameTab.PIPELINE
                                2 -> NavGameTab.TIM_ALAT
                                3 -> NavGameTab.KEUANGAN
                                else -> NavGameTab.COCKPIT_3D
                            }
                        },
                        onOpenErpMode = { modeKey ->
                            when (modeKey) {
                                "COCKPIT" -> syncAndSwitchMode(WorkspaceMode.ERP_COCKPIT)
                                "OPERASI" -> syncAndSwitchMode(WorkspaceMode.ERP_OPERASI)
                                "KEUANGAN" -> syncAndSwitchMode(WorkspaceMode.ERP_KEUANGAN)
                                "SIMULATOR" -> syncAndSwitchMode(WorkspaceMode.ERP_SIMULATOR)
                                "SETTING" -> syncAndSwitchMode(WorkspaceMode.ERP_SETTING)
                            }
                        }
                    )
                }
                WorkspaceMode.ERP_COCKPIT -> {
                    CockpitScreen(
                        state = erpState,
                        onOpenAddOrder = { erpViewModel.openAddOrderSheet() },
                        onNavigateToOperasi = {
                            syncAndSwitchMode(WorkspaceMode.ERP_OPERASI)
                        },
                        onNavigateToSimulator = {
                            syncAndSwitchMode(WorkspaceMode.ERP_SIMULATOR)
                        }
                    )
                }
                WorkspaceMode.ERP_OPERASI -> {
                    OperasiScreen(
                        state = erpState,
                        onAdvanceOrderStatus = { erpViewModel.advanceOrderStatus(it) },
                        onDeleteOrder = { erpViewModel.deleteOrder(it) },
                        onOpenAddOrder = { erpViewModel.openAddOrderSheet() }
                    )
                }
                WorkspaceMode.ERP_KEUANGAN -> {
                    KeuanganScreen(
                        state = erpState
                    )
                }
                WorkspaceMode.ERP_SIMULATOR -> {
                    SimulatorScreen(
                        state = erpState,
                        onUpdateOrderKg = { erpViewModel.updateSimOrderKg(it) },
                        onUpdatePrice = { erpViewModel.updateSimPrice(it) },
                        onUpdateStaff = { erpViewModel.updateSimStaff(it) },
                        onUpdateWashingMachines = { erpViewModel.updateSimWashingMachines(it) },
                        onUpdateDryers = { erpViewModel.updateSimDryers(it) },
                        onUpdateRent = { erpViewModel.updateSimRent(it) },
                        onUpdateElectricity = { erpViewModel.updateSimElectricity(it) },
                        onUpdateSalary = { erpViewModel.updateSimSalary(it) },
                        onApplyScenario = { erpViewModel.applyScenario(it) },
                        onUpdateMachineInvestmentInput = { erpViewModel.updateMachineInvestmentInput(it) },
                        onUpdateTargetProfit = { erpViewModel.updateTargetProfit(it) },
                        onUpdatePricingMargin = { erpViewModel.updatePricingTargetMargin(it) },
                        onUpdatePricingCompetitor = { erpViewModel.updatePricingCompetitor(it) }
                    )
                }
                WorkspaceMode.ERP_SETTING -> {
                    SettingScreen(
                        state = erpState,
                        onSaveConfig = { erpViewModel.updateConfig(it) },
                        onResetToDefault = { erpViewModel.resetToSampleData() }
                    )
                }
            }

            if (erpState.isAddOrderSheetOpen) {
                AddOrderBottomSheet(
                    config = erpState.config,
                    onDismiss = { erpViewModel.closeAddOrderSheet() },
                    onSubmit = { name, phone, service, weight, price, deadline, status ->
                        erpViewModel.addOrder(name, phone, service, weight, price, deadline, status)
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkspaceModeSwitcherBar(
    currentMode: WorkspaceMode,
    onSelectMode: (WorkspaceMode) -> Unit,
    onSyncGameToErp: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkspaceMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(listOf(mode.primaryColor, mode.secondaryColor))
                            else Brush.linearGradient(listOf(Slate100, Slate100))
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) mode.primaryColor else Slate200,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag(mode.tag)
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Slate700,
                        maxLines = 1
                    )
                }
            }

            // Clean 1-Tap Sync Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MintTintBg)
                    .border(1.dp, MintEmerald, RoundedCornerShape(10.dp))
                    .clickable { onSyncGameToErp() }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
                    .testTag("btn_sync_game_to_erp")
            ) {
                Text(
                    text = "🔄 Sync Data",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MintEmeraldDark,
                    maxLines = 1
                )
            }
        }
    }
}
