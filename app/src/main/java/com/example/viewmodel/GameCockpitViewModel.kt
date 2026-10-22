package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BusinessConfig
import com.example.model.LaundryOrder
import com.example.model.OrderStatus
import com.example.model.ServiceType
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
    val currentTab: String = "COCKPIT", // COCKPIT, PIPELINE, TIM, KEUANGAN
    val stateRevision: Long = 0L // Ensures MutableStateFlow always emits on in-place engine mutations
)

class GameCockpitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        // Start simulation loop ticker
        startSimulationTicker()
    }

    private fun emitEngineUpdate(transform: (GameUiState) -> GameUiState = { it }) {
        _uiState.update { current ->
            transform(current).copy(stateRevision = current.stateRevision + 1L)
        }
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
                        emitEngineUpdate { it.copy(isEveningSummaryOpen = true, latestDayReport = report) }
                    } else {
                        emitEngineUpdate()
                    }
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
        emitEngineUpdate()
    }

    // Station inspection
    fun selectStationPhase(phase: StationPhase) {
        emitEngineUpdate { it.copy(selectedPhase = phase, isStationInspectorOpen = true) }
    }

    fun closeStationInspector() {
        emitEngineUpdate { it.copy(isStationInspectorOpen = false, selectedPhase = null) }
    }

    // Reassign worker
    fun reassignWorker(workerId: String, newPhase: StationPhase) {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val worker = activeEngine.workerList.find { it.id == workerId }
        if (worker != null) {
            worker.assignedPhase = newPhase
            emitEngineUpdate()
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
        emitEngineUpdate()
    }

    // Boosters
    fun giveCoffeeBoost(): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.giveCoffeeBoost()
        emitEngineUpdate()
        return success
    }

    fun activateTurboWash(): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.activateTurboWash()
        emitEngineUpdate()
        return success
    }

    fun triggerPromoFlyer(): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.triggerPromoFlyer()
        emitEngineUpdate()
        return success
    }

    fun claimQuest(questId: String): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.claimQuest(questId)
        emitEngineUpdate()
        return success
    }

    // Shop actions
    fun openShop() {
        emitEngineUpdate { it.copy(isShopOpen = true) }
    }

    fun closeShop() {
        emitEngineUpdate { it.copy(isShopOpen = false) }
    }

    fun buyEquipment(assetType: AssetType): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.buyEquipment(assetType)
        emitEngineUpdate()
        return success
    }

    fun hireWorker(name: String, phase: StationPhase): Boolean {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val success = activeEngine.hireWorker(name, phase)
        emitEngineUpdate()
        return success
    }

    // Manual close day
    fun manualCloseDay() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.simSpeed = SimSpeed.PAUSED
        val report = activeEngine.closeDayAndGenerateReport()
        emitEngineUpdate { it.copy(isEveningSummaryOpen = true, latestDayReport = report) }
    }

    // Day Cycle Handlers
    fun startMorningPlanning() {
        emitEngineUpdate { it.copy(isMorningPlanningOpen = true) }
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
        emitEngineUpdate { it.copy(isMorningPlanningOpen = false) }
    }

    fun continueToNextDay() {
        val state = _uiState.value
        state.engine.startNextDay()
        emitEngineUpdate {
            it.copy(
                isEveningSummaryOpen = false,
                isMorningPlanningOpen = true
            )
        }
    }

    // Lab Mode Sandbox
    fun openLabModal() {
        emitEngineUpdate { it.copy(isLabModalOpen = true) }
    }

    fun closeLabModal() {
        emitEngineUpdate { it.copy(isLabModalOpen = false) }
    }

    fun enterLabMode() {
        val clone = _uiState.value.engine.cloneForLab()
        emitEngineUpdate { it.copy(labEngine = clone, isLabModeActive = true) }
    }

    fun exitLabMode() {
        emitEngineUpdate { it.copy(labEngine = null, isLabModeActive = false) }
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
        emitEngineUpdate {
            it.copy(
                labEngine = null,
                isLabModeActive = false,
                isLabModalOpen = false
            )
        }
    }

    fun closeOnboarding() {
        emitEngineUpdate { it.copy(isOnboardingOpen = false) }
    }

    fun openOnboarding() {
        emitEngineUpdate { it.copy(isOnboardingOpen = true) }
    }

    fun setBottomTab(tab: String) {
        emitEngineUpdate { it.copy(currentTab = tab) }
    }

    // Laundry Experience Handlers
    fun interactWithStation(phase: StationPhase) {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.interactWithStation(phase)
        emitEngineUpdate()
    }

    fun claimLevelUp() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.claimLevelUp()
        emitEngineUpdate()
    }

    fun dismissStreakCelebration() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.dismissStreakCelebration()
        emitEngineUpdate()
    }

    fun triggerTestLevelUp() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.triggerTestLevelUp()
        emitEngineUpdate()
    }

    fun triggerTestStreak() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.triggerTestStreak()
        emitEngineUpdate()
    }

    fun openZeroLateStreakModal() {
        emitEngineUpdate { it.copy(isZeroLateStreakModalOpen = true) }
    }

    fun closeZeroLateStreakModal() {
        emitEngineUpdate { it.copy(isZeroLateStreakModalOpen = false) }
    }

    fun claimVisualSatisfactionBonus() {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        activeEngine.claimVisualSatisfactionBonus()
        emitEngineUpdate()
    }

    /**
     * Converts the live 3D Tycoon SimulationEngine state into a BusinessConfig
     * so the user can seamlessly analyze their 3D game laundry in the ERP Business Simulator.
     */
    fun toBusinessConfig(): BusinessConfig {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        val washers = activeEngine.equipmentList.filter { it.type == AssetType.MESIN_CUCI_7KG || it.type == AssetType.MESIN_CUCI_14KG }
        val dryers = activeEngine.equipmentList.filter { it.type == AssetType.DRYER_GAS_10KG }
        val irons = activeEngine.equipmentList.filter { it.type == AssetType.SETRIKA_LISTRIK || it.type == AssetType.SETRIKA_UAP_BOILER }

        val avgWashCap = if (washers.isNotEmpty()) washers.map { it.capacityKg }.average() else 7.0
        val avgWashCycle = if (washers.isNotEmpty()) washers.map { it.cycleMinutes.toDouble() }.average() else 45.0
        val hasBoiler = irons.any { it.type == AssetType.SETRIKA_UAP_BOILER }

        return BusinessConfig(
            businessName = "${activeEngine.growthTier.title} (Lv.${activeEngine.tycoonLevel})",
            regularPricePerKg = 9_000.0,
            expressPricePerKg = 15_000.0,
            targetKgPerDay = activeEngine.growthTier.maxDailyCapacityTargetKg,
            operatingHoursPerDay = if (activeEngine.isOvertimeActiveToday) 12.0 else 10.0,
            operatingEfficiency = 0.80,
            washingMachineCount = washers.size.coerceAtLeast(1),
            washingCapacityPerMachineKg = avgWashCap,
            washingCycleDurationMinutes = avgWashCycle,
            dryerCount = dryers.size,
            dryerCapacityPerMachineKg = 10.0,
            dryerCycleDurationMinutes = 45.0,
            ironCount = irons.size.coerceAtLeast(1),
            ironCapacityPerUnitPerHourKg = if (hasBoiler) 15.0 else 8.0,
            staffCount = activeEngine.workerList.size.coerceAtLeast(1),
            salaryPerStaffMonthly = 2_700_000.0,
            initialCash = activeEngine.cash.coerceAtLeast(500_000.0)
        )
    }

    /**
     * Maps live 3D simulation orders into ERP LaundryOrder items.
     */
    fun toBusinessOrders(): List<LaundryOrder> {
        val state = _uiState.value
        val activeEngine = if (state.isLabModeActive && state.labEngine != null) state.labEngine else state.engine
        return activeEngine.ordersList.map { simOrder ->
            val mappedStatus = when (simOrder.currentPhase) {
                StationPhase.MASUK -> OrderStatus.MASUK
                StationPhase.CUCI -> OrderStatus.CUCI
                StationPhase.JEMUR_KERING -> OrderStatus.KERING
                StationPhase.SETRIKA -> OrderStatus.SETRIKA
                StationPhase.PACKING -> OrderStatus.PACKING
                StationPhase.SELESAI -> if (simOrder.isDelivered) OrderStatus.DIAMBIL else OrderStatus.SELESAI
            }
            val service = if (simOrder.isExpress) ServiceType.EXPRESS else ServiceType.REGULER
            val pricePerKg = if (simOrder.isExpress) 15_000.0 else 9_000.0
            LaundryOrder(
                id = simOrder.id,
                customerName = simOrder.customerName,
                serviceType = service,
                weightKg = simOrder.weightKg,
                pricePerKg = pricePerKg,
                totalPrice = simOrder.priceTotal,
                deadline = if (simOrder.deadlineDay <= activeEngine.currentDay) "Hari ini" else "Besok",
                status = mappedStatus
            )
        }
    }
}
