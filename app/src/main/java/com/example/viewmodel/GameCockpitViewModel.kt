package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sim.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
    val engine: SimulationEngine = SimulationEngine(),
    val labEngine: SimulationEngine? = null,
    val isLabModeActive: Boolean = false,
    val selectedPhase: StationPhase? = null,

    // Dialog & UI Visibility
    val isOnboardingOpen: Boolean = false,
    val isMorningPlanningOpen: Boolean = false,
    val isEveningSummaryOpen: Boolean = false,
    val isStationInspectorOpen: Boolean = false,
    val isShopOpen: Boolean = false,
    val isLabModalOpen: Boolean = false,
    val isZeroLateStreakModalOpen: Boolean = false,

    val latestDayReport: DailySummaryReport? = null,
    val currentTab: String = "COCKPIT" // COCKPIT, PIPELINE, TIM, KEUANGAN
)

class GameCockpitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        // Start simulation loop ticker
        startSimulationTicker()
    }

    private fun startSimulationTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine

                if (activeEngine.simSpeed != SimSpeed.PAUSED && !state.isMorningPlanningOpen && !state.isEveningSummaryOpen) {
                    val stepMinutes = activeEngine.simSpeed.multiplier
                    activeEngine.tickMinutes(stepMinutes)

                    // Check if day has ended
                    val closeMinute = if (activeEngine.isOvertimeActiveToday) activeEngine.overtimeCloseWorkMinute else activeEngine.normalCloseWorkMinute
                    if (activeEngine.currentMinuteOfDay >= closeMinute) {
                        activeEngine.simSpeed = SimSpeed.PAUSED
                        val report = activeEngine.closeDayAndGenerateReport()
                        _uiState.update { it.copy(isEveningSummaryOpen = true, latestDayReport = report) }
                    }

                    // Trigger recomposition by updating state reference
                    _uiState.update { it.copy() }
                }

                delay(400) // 400ms tick interval
            }
        }
    }

    // Engine speed control
    fun setSpeed(speed: SimSpeed) {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.simSpeed = speed
        _uiState.update { it.copy() }
    }

    // Station inspection
    fun selectStationPhase(phase: StationPhase) {
        _uiState.update { it.copy(selectedPhase = phase, isStationInspectorOpen = true) }
    }

    fun closeStationInspector() {
        _uiState.update { it.copy(isStationInspectorOpen = false, selectedPhase = null) }
    }

    // Reassign worker
    fun reassignWorker(workerId: String, newPhase: StationPhase) {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val worker = activeEngine.workerList.find { it.id == workerId }
        if (worker != null) {
            worker.assignedPhase = newPhase
            _uiState.update { it.copy() }
        }
    }

    // Toggle Overtime
    fun toggleOvertime() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.isOvertimeActiveToday = !activeEngine.isOvertimeActiveToday
        for (w in activeEngine.workerList) {
            w.isOvertime = activeEngine.isOvertimeActiveToday
        }
        _uiState.update { it.copy() }
    }

    // Boosters
    fun giveCoffeeBoost(): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.giveCoffeeBoost()
        _uiState.update { it.copy() }
        return success
    }

    fun activateTurboWash(): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.activateTurboWash()
        _uiState.update { it.copy() }
        return success
    }

    fun triggerPromoFlyer(): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.triggerPromoFlyer()
        _uiState.update { it.copy() }
        return success
    }

    fun claimQuest(questId: String): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.claimQuest(questId)
        _uiState.update { it.copy() }
        return success
    }

    // Shop actions
    fun openShop() {
        _uiState.update { it.copy(isShopOpen = true) }
    }

    fun closeShop() {
        _uiState.update { it.copy(isShopOpen = false) }
    }

    fun buyEquipment(assetType: AssetType): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.buyEquipment(assetType)
        _uiState.update { it.copy() }
        return success
    }

    fun hireWorker(name: String, phase: StationPhase): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.hireWorker(name, phase)
        _uiState.update { it.copy() }
        return success
    }

    // Manual close day
    fun manualCloseDay() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.simSpeed = SimSpeed.PAUSED
        val report = activeEngine.closeDayAndGenerateReport()
        _uiState.update { it.copy(isEveningSummaryOpen = true, latestDayReport = report) }
    }

    // Day Cycle Handlers
    fun startMorningPlanning() {
        _uiState.update { it.copy(isMorningPlanningOpen = true) }
    }

    fun confirmMorningPlan(maxQuotaKg: Double, isOvertime: Boolean) {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.maxDailyIntakeKgQuota = maxQuotaKg
        activeEngine.isOvertimeActiveToday = isOvertime
        for (w in activeEngine.workerList) {
            w.isOvertime = isOvertime
        }
        activeEngine.simSpeed = SimSpeed.SPEED_1X
        _uiState.update { it.copy(isMorningPlanningOpen = false) }
    }

    fun continueToNextDay() {
        val state = _uiState.value
        state.engine.startNextDay()
        _uiState.update {
            it.copy(
                isEveningSummaryOpen = false,
                isMorningPlanningOpen = true
            )
        }
    }

    // Lab Mode Sandbox
    fun openLabModal() {
        _uiState.update { it.copy(isLabModalOpen = true) }
    }

    fun closeLabModal() {
        _uiState.update { it.copy(isLabModalOpen = false) }
    }

    fun enterLabMode() {
        val clone = _uiState.value.engine.cloneForLab()
        _uiState.update { it.copy(labEngine = clone, isLabModeActive = true) }
    }

    fun exitLabMode() {
        _uiState.update { it.copy(labEngine = null, isLabModeActive = false) }
    }

    fun applyLabDecisionToActive(dryerBought: Boolean, staffHired: Boolean, extraOvertime: Boolean) {
        val engine = _uiState.value.engine
        if (dryerBought) {
            engine.buyEquipment(AssetType.DRYER_GAS_10KG)
        }
        if (staffHired) {
            engine.hireWorker("Bambang", StationPhase.SETRIKA)
        }
        if (extraOvertime) {
            engine.isOvertimeActiveToday = true
            for (w in engine.workerList) w.isOvertime = true
        }
        _uiState.update {
            it.copy(
                labEngine = null,
                isLabModeActive = false,
                isLabModalOpen = false
            )
        }
    }

    fun closeOnboarding() {
        _uiState.update { it.copy(isOnboardingOpen = false) }
    }

    fun openOnboarding() {
        _uiState.update { it.copy(isOnboardingOpen = true) }
    }

    fun setBottomTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    // Laundry Experience Handlers
    fun interactWithStation(phase: StationPhase) {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.interactWithStation(phase)
        _uiState.update { it.copy() }
    }

    fun claimLevelUp() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.claimLevelUp()
        _uiState.update { it.copy() }
    }

    fun dismissStreakCelebration() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.dismissStreakCelebration()
        _uiState.update { it.copy() }
    }

    fun triggerTestLevelUp() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.triggerTestLevelUp()
        _uiState.update { it.copy() }
    }

    fun openZeroLateStreakModal() {
        _uiState.update { it.copy(isZeroLateStreakModalOpen = true) }
    }

    fun closeZeroLateStreakModal() {
        _uiState.update { it.copy(isZeroLateStreakModalOpen = false) }
    }

    fun claimVisualSatisfactionBonus() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.claimVisualSatisfactionBonus()
        _uiState.update { it.copy() }
    }
}
