package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class CockpitUiState(
    val config: BusinessConfig = SampleData.defaultBusinessConfig,
    val orders: List<LaundryOrder> = SampleData.getInitialOrders(),

    // Simulator State
    val simOrderKgPerDay: Double = 100.0,
    val simPricePerKg: Double = 9_000.0,
    val simStaffCount: Int = 4,
    val simWashingMachines: Int = 3,
    val simDryers: Int = 2,
    val simRentMonthly: Double = 6_000_000.0,
    val simElectricityMonthly: Double = 3_500_000.0,
    val simSalaryPerStaff: Double = 3_000_000.0,
    val activeScenario: ScenarioPreset = ScenarioPreset.NORMAL,
    val scenarioImpactText: String? = null,
    val scenarioWarningText: String? = null,

    // Machine Investment Simulator
    val machineInvestmentInput: MachineInvestmentInput = MachineInvestmentInput(),

    // Target Profit Calculator
    val targetProfitMonthly: Double = 20_000_000.0,

    // Pricing Simulator
    val pricingTargetMarginPercent: Double = 30.0,
    val pricingCompetitorPrice: Double = 9_500.0,

    // Dialog & UI controls
    val isAddOrderSheetOpen: Boolean = false,
    val selectedPipelineFilter: OrderStatus? = null
)

class LaundryCockpitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CockpitUiState())
    val uiState: StateFlow<CockpitUiState> = _uiState.asStateFlow()

    fun openAddOrderSheet() {
        _uiState.update { it.copy(isAddOrderSheetOpen = true) }
    }

    fun closeAddOrderSheet() {
        _uiState.update { it.copy(isAddOrderSheetOpen = false) }
    }

    fun setPipelineFilter(status: OrderStatus?) {
        _uiState.update { it.copy(selectedPipelineFilter = status) }
    }

    fun addOrder(
        customerName: String,
        phone: String,
        serviceType: ServiceType,
        weightKg: Double,
        pricePerKg: Double,
        deadline: String,
        status: OrderStatus
    ) {
        val newOrder = LaundryOrder(
            id = "ORD-${System.currentTimeMillis().toString().takeLast(4)}",
            customerName = customerName.ifBlank { "Pelanggan Baru" },
            phone = phone,
            serviceType = serviceType,
            weightKg = weightKg,
            pricePerKg = pricePerKg,
            totalPrice = weightKg * pricePerKg,
            deadline = deadline,
            status = status
        )
        _uiState.update { state ->
            state.copy(
                orders = listOf(newOrder) + state.orders,
                isAddOrderSheetOpen = false
            )
        }
    }

    fun advanceOrderStatus(orderId: String) {
        _uiState.update { state ->
            val updated = state.orders.map { order ->
                if (order.id == orderId) {
                    val next = order.status.nextStatus()
                    if (next != null) order.copy(status = next) else order
                } else order
            }
            state.copy(orders = updated)
        }
    }

    fun deleteOrder(orderId: String) {
        _uiState.update { state ->
            state.copy(orders = state.orders.filterNot { it.id == orderId })
        }
    }

    fun resetToSampleData() {
        _uiState.update {
            CockpitUiState(
                config = SampleData.defaultBusinessConfig,
                orders = SampleData.getInitialOrders()
            )
        }
    }

    // Config Update Handlers
    fun updateConfig(newConfig: BusinessConfig) {
        _uiState.update { state ->
            state.copy(
                config = newConfig,
                simOrderKgPerDay = newConfig.targetKgPerDay,
                simPricePerKg = newConfig.regularPricePerKg,
                simStaffCount = newConfig.staffCount,
                simWashingMachines = newConfig.washingMachineCount,
                simDryers = newConfig.dryerCount,
                simRentMonthly = newConfig.rentMonthly,
                simElectricityMonthly = newConfig.electricityMonthlyBase,
                simSalaryPerStaff = newConfig.salaryPerStaffMonthly
            )
        }
    }

    // Simulator Sliders
    fun updateSimOrderKg(value: Double) {
        _uiState.update { it.copy(simOrderKgPerDay = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimPrice(value: Double) {
        _uiState.update { it.copy(simPricePerKg = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimStaff(value: Int) {
        _uiState.update { it.copy(simStaffCount = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimWashingMachines(value: Int) {
        _uiState.update { it.copy(simWashingMachines = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimDryers(value: Int) {
        _uiState.update { it.copy(simDryers = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimRent(value: Double) {
        _uiState.update { it.copy(simRentMonthly = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimElectricity(value: Double) {
        _uiState.update { it.copy(simElectricityMonthly = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun updateSimSalary(value: Double) {
        _uiState.update { it.copy(simSalaryPerStaff = value, activeScenario = ScenarioPreset.NORMAL, scenarioImpactText = null, scenarioWarningText = null) }
    }

    fun applyScenario(preset: ScenarioPreset) {
        val baseConfig = _uiState.value.config
        var orderKg = baseConfig.targetKgPerDay
        var price = baseConfig.regularPricePerKg
        var staff = baseConfig.staffCount
        var wash = baseConfig.washingMachineCount
        var dry = baseConfig.dryerCount
        var electricity = baseConfig.electricityMonthlyBase
        var salary = baseConfig.salaryPerStaffMonthly
        val rent = baseConfig.rentMonthly

        var impactText = ""
        var warningText: String? = null

        when (preset) {
            ScenarioPreset.NORMAL -> {
                impactText = "Kembali ke parameter standar normal."
            }
            ScenarioPreset.ORDER_UP_20 -> {
                orderKg *= 1.20
                impactText = "Order naik +20% (menjadi ${orderKg.roundToInt()} kg/hari). Omzet & laba meningkat!"
            }
            ScenarioPreset.ORDER_UP_50 -> {
                orderKg *= 1.50
                impactText = "Order melonjak +50% (menjadi ${orderKg.roundToInt()} kg/hari)."
                warningText = "⚠️ Kapasitas mesin & setrika tidak mencukupi, risiko antrian menumpuk!"
            }
            ScenarioPreset.ORDER_DOWN_20 -> {
                orderKg *= 0.80
                impactText = "Order turun -20% (menjadi ${orderKg.roundToInt()} kg/hari)."
                warningText = "⚠️ Utilisasi turun & profit tertekan mendekati titik BEP."
            }
            ScenarioPreset.PRICE_UP_10 -> {
                price *= 1.10
                impactText = "Harga dinaikkan 10% (menjadi ${formatRupiah(price)}/kg). Margin naik signifikan!"
            }
            ScenarioPreset.SALARY_UP_10 -> {
                salary *= 1.10
                impactText = "Gaji naik 10% (menjadi ${formatRupiah(salary)}/orang). Beban tetap bertambah."
            }
            ScenarioPreset.ELECTRICITY_UP_20 -> {
                electricity *= 1.20
                impactText = "Tarif listrik naik 20% (menjadi ${formatRupiah(electricity)}/bulan)."
            }
            ScenarioPreset.MACHINE_BROKEN -> {
                wash = (wash - 1).coerceAtLeast(1)
                impactText = "1 Mesin cuci rusak (sisa $wash unit operasional)."
                warningText = "⚠️ Kapasitas cuci turun tajam! Terjadi bottleneck berat pada proses cuci."
            }
            ScenarioPreset.ADD_MACHINE -> {
                wash += 1
                impactText = "Beli 1 mesin cuci baru (total $wash unit). Kapasitas bertambah."
            }
            ScenarioPreset.ADD_STAFF -> {
                staff += 1
                impactText = "Rekrut 1 karyawan baru (total $staff orang). Kapasitas setrika bertambah."
            }
            ScenarioPreset.EXPRESS_SURGE -> {
                // Higher average revenue per kg
                price = baseConfig.regularPricePerKg * 1.25
                impactText = "Order express mendominasi, rata-rata omzet/kg meningkat tajam."
            }
        }

        _uiState.update {
            it.copy(
                activeScenario = preset,
                simOrderKgPerDay = orderKg,
                simPricePerKg = price,
                simStaffCount = staff,
                simWashingMachines = wash,
                simDryers = dry,
                simElectricityMonthly = electricity,
                simSalaryPerStaff = salary,
                simRentMonthly = rent,
                scenarioImpactText = impactText,
                scenarioWarningText = warningText
            )
        }
    }

    // Machine Investment Inputs
    fun updateMachineInvestmentInput(newInput: MachineInvestmentInput) {
        _uiState.update { it.copy(machineInvestmentInput = newInput) }
    }

    // Target Profit Inputs
    fun updateTargetProfit(targetMonthly: Double) {
        _uiState.update { it.copy(targetProfitMonthly = targetMonthly) }
    }

    // Pricing Simulator Inputs
    fun updatePricingTargetMargin(margin: Double) {
        _uiState.update { it.copy(pricingTargetMarginPercent = margin) }
    }

    fun updatePricingCompetitor(compPrice: Double) {
        _uiState.update { it.copy(pricingCompetitorPrice = compPrice) }
    }
}
