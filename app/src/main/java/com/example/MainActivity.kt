package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sim.StationPhase
import com.example.ui.screens.MainGameCockpitScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameCockpitViewModel

enum class NavGameTab(val title: String, val icon: ImageVector, val tag: String) {
    COCKPIT_3D("3D Cockpit", Icons.Default.ViewInAr, "tab_3d_cockpit"),
    PIPELINE("Pipeline", Icons.Default.LinearScale, "tab_pipeline"),
    TIM_ALAT("Tim & Alat", Icons.Default.Engineering, "tab_tim_alat"),
    KEUANGAN("Keuangan", Icons.Default.AccountBalanceWallet, "tab_keuangan"),
    PANDUAN("Panduan", Icons.Default.HelpOutline, "tab_panduan")
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
    viewModel: GameCockpitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentNavTab by remember { mutableStateOf(NavGameTab.COCKPIT_3D) }

    // If back is pressed on another tab, return to main 3D Cockpit
    BackHandler(enabled = currentNavTab != NavGameTab.COCKPIT_3D) {
        currentNavTab = NavGameTab.COCKPIT_3D
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavGameTab.values().forEach { tab ->
                    val isSelected = currentNavTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab == NavGameTab.PANDUAN) {
                                viewModel.openOnboarding()
                            } else {
                                currentNavTab = tab
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF0284C7).copy(alpha = 0.25f),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
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
                .background(Color(0xFF0F172A))
        ) {
            when (currentNavTab) {
                NavGameTab.COCKPIT_3D -> {
                    MainGameCockpitScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                NavGameTab.PIPELINE -> {
                    // Navigate directly with station inspection ready
                    MainGameCockpitScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                NavGameTab.TIM_ALAT -> {
                    MainGameCockpitScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                NavGameTab.KEUANGAN -> {
                    MainGameCockpitScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                NavGameTab.PANDUAN -> {
                    MainGameCockpitScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}
