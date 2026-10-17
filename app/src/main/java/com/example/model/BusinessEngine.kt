package com.example.model

import com.example.viewmodel.CockpitUiState
import kotlin.math.max
import kotlin.math.roundToInt

data class DailyCockpitSummary(
    val orderCountToday: Int,
    val kgMasukToday: Double,
    val kgSelesaiToday: Double,
    val kgBelumSelesaiToday: Double,
    val revenueToday: Double,
    val costToday: Double,
    val profitToday: Double,
    val marginPercent: Double,
    val capacityUtilizationPercent: Double,
    val healthIndicators: List<HealthIndicator>,
    val attentionMessage: String,
    val dailyDecisions: List<String>,
    val targetKgPerDay: Double,
    val actualKgPerDay: Double,
    val progressPercent: Double
)

data class PipelineSummary(
    val masukKg: Double,
    val cuciKg: Double,
    val keringKg: Double,
    val setrikaKg: Double,
    val packingKg: Double,
    val selesaiKg: Double,
    val bottleneck: BottleneckAnalysis
)

data class CashflowForecastItem(
    val periodLabel: String,
    val days: Int,
    val projectedRevenue: Double,
    val projectedExpenses: Double,
    val projectedNetCash: Double,
    val endingCashBalance: Double
)

data class CustomerEconomics(
    val activeCustomers: Int,
    val avgTransactionRp: Double,
    val avgKgPerCustomer: Double,
    val monthlyFrequency: Double,
    val revenuePerCustomerMonthly: Double,
    val simpleCLV: Double,
    val repeatCustomerRate: Double,
    val newCustomersCount: Int,
    val lostCustomersCount: Int
)

data class SimulatorCalculations(
    val dailyRevenue: Double,
    val monthlyRevenue: Double,
    val dailyVariableCost: Double,
    val monthlyFixedCost: Double,
    val totalMonthlyCost: Double,
    val dailyProfit: Double,
    val monthlyProfit: Double,
    val marginPercent: Double,
    val capacityUtilizationPercent: Double,
    val maxDailyPracticalCapacityKg: Double,
    val bepKgPerDay: Double,
    val bepKgPerMonth: Double,
    val monthlyCashflow: Double,
    val annualROI: Double,
    val isOverCapacity: Boolean
)

object BusinessEngine {

    fun computeDailySummary(state: CockpitUiState): DailyCockpitSummary {
        val config = state.config
        val orders = state.orders

        // Orders today (filter non-taken or recent)
        val activeOrders = orders.filter { it.status != OrderStatus.DIAMBIL }
        val orderCount = activeOrders.size
        val kgMasuk = activeOrders.sumOf { it.weightKg }
        val kgSelesai = orders.filter { it.status == OrderStatus.SELESAI }.sumOf { it.weightKg }
        val kgBelumSelesai = activeOrders.filter { it.status != OrderStatus.SELESAI }.sumOf { it.weightKg }

        // Revenue today
        val revenueToday = activeOrders.sumOf { it.totalPrice }
        
        // Cost today = Daily pure fixed cost + (Variable cost per kg * kgMasuk)
        val dailyFixedCost = config.totalPureFixedCostMonthly / 30.0
        val dailyVariableCost = config.variableCostPerKg * kgMasuk
        val costToday = dailyFixedCost + dailyVariableCost
        val profitToday = revenueToday - costToday
        val marginPercent = if (revenueToday > 0) (profitToday / revenueToday) * 100.0 else 0.0

        val maxPracticalCapacity = config.maxDailyPracticalCapacityKg
        val capacityUtilization = if (maxPracticalCapacity > 0) (kgMasuk / maxPracticalCapacity) * 100.0 else 0.0

        // Health Indicators
        val healthIndicators = mutableListOf<HealthIndicator>()
        if (profitToday >= 0) {
            healthIndicators.add(HealthIndicator("PROFIT", HealthStatus.GREEN, "${formatRupiah(profitToday)} / hari"))
        } else {
            healthIndicators.add(HealthIndicator("RUGI", HealthStatus.RED, "${formatRupiah(profitToday)} / hari"))
        }

        if (capacityUtilization > 90.0) {
            healthIndicators.add(HealthIndicator("KAPASITAS ${capacityUtilization.roundToInt()}%", HealthStatus.RED, "Mendekati limit maksimum"))
        } else if (capacityUtilization > 75.0) {
            healthIndicators.add(HealthIndicator("KAPASITAS ${capacityUtilization.roundToInt()}%", HealthStatus.YELLOW, "Tinggi, waspadai antrian"))
        } else {
            healthIndicators.add(HealthIndicator("KAPASITAS ${capacityUtilization.roundToInt()}%", HealthStatus.GREEN, "Kapasitas aman"))
        }

        if (kgBelumSelesai > 40.0) {
            healthIndicators.add(HealthIndicator("ANTRIAN ${kgBelumSelesai.roundToInt()} KG", HealthStatus.RED, "Perlu percepatan proses"))
        } else if (kgBelumSelesai > 20.0) {
            healthIndicators.add(HealthIndicator("ANTRIAN ${kgBelumSelesai.roundToInt()} KG", HealthStatus.YELLOW, "Antrian sedang berjalan"))
        } else {
            healthIndicators.add(HealthIndicator("ANTRIAN ${kgBelumSelesai.roundToInt()} KG", HealthStatus.GREEN, "Lancar"))
        }

        healthIndicators.add(HealthIndicator("CASHFLOW AMAN", HealthStatus.GREEN, "Kas: ${formatRupiahCompact(config.initialCash)}"))

        // Attention Message
        val attentionMessage = when {
            capacityUtilization >= 90.0 ->
                "Mesin & operasional hampir mencapai kapasitas maksimum (${capacityUtilization.roundToInt()}%). Jangan menerima order express berlebihan hari ini."
            kgBelumSelesai > 35.0 ->
                "Jika order bertambah 25 kg hari ini, penyelesaian berpotensi terlambat akibat antrian setrika."
            profitToday < 0 ->
                "Omzet hari ini belum mencapai titik BEP harian. Perlu minimal ${formatKg(computeBEP(config).gapKgPerDay * -1)} lagi."
            else ->
                "Operasi hari ini berjalan sangat optimal. Margin laba tercatat ${marginPercent.roundToInt()}% dengan cashflow stabil."
        }

        // 3 Decisions of the Day
        val decisions = mutableListOf<String>()
        if (kgBelumSelesai > 30.0) {
            decisions.add("Prioritaskan ${kgBelumSelesai.roundToInt()} kg laundry yang menumpuk di stasiun setrika & packing.")
        } else {
            decisions.add("Jaga kecepatan pengerjaan order reguler agar siap sebelum jam 17:00.")
        }

        if (capacityUtilization > 85.0) {
            decisions.add("Batasi penerimaan order Express tambahan hari ini (maks 15 kg).")
        } else {
            decisions.add("Kapasitas masih longgar (${(100 - capacityUtilization).roundToInt()}% sisa), tawarkan promo express kepada pelanggan setia.")
        }

        decisions.add("Tidak perlu menambah mesin cuci; fokus perkuat produktivitas meja setrika.")

        val target = config.targetKgPerDay
        val progress = if (target > 0) ((kgMasuk / target) * 100.0).coerceIn(0.0, 150.0) else 0.0

        return DailyCockpitSummary(
            orderCountToday = orderCount,
            kgMasukToday = kgMasuk,
            kgSelesaiToday = kgSelesai,
            kgBelumSelesaiToday = kgBelumSelesai,
            revenueToday = revenueToday,
            costToday = costToday,
            profitToday = profitToday,
            marginPercent = marginPercent,
            capacityUtilizationPercent = capacityUtilization,
            healthIndicators = healthIndicators,
            attentionMessage = attentionMessage,
            dailyDecisions = decisions.take(3),
            targetKgPerDay = target,
            actualKgPerDay = kgMasuk,
            progressPercent = progress
        )
    }

    fun computePipeline(state: CockpitUiState): PipelineSummary {
        val orders = state.orders
        val masukKg = orders.filter { it.status == OrderStatus.MASUK }.sumOf { it.weightKg }
        val cuciKg = orders.filter { it.status == OrderStatus.CUCI }.sumOf { it.weightKg }
        val keringKg = orders.filter { it.status == OrderStatus.KERING }.sumOf { it.weightKg }
        val setrikaKg = orders.filter { it.status == OrderStatus.SETRIKA }.sumOf { it.weightKg }
        val packingKg = orders.filter { it.status == OrderStatus.PACKING }.sumOf { it.weightKg }
        val selesaiKg = orders.filter { it.status == OrderStatus.SELESAI }.sumOf { it.weightKg }

        // Analyze Bottleneck
        // Effective setrika capacity: Assume 1-2 staff assigned to iron, yielding ~60 - 80 kg/day
        val setrikaCapacityKg = (state.config.ironCount * state.config.ironCapacityPerUnitPerHourKg * 4.0 * state.config.operatingEfficiency).coerceAtLeast(60.0)
        val setrikaLoadKg = setrikaKg + keringKg * 0.8 // incoming load
        val setrikaUtil = (setrikaLoadKg / setrikaCapacityKg) * 100.0
        val setrikaBacklog = max(0.0, setrikaLoadKg - setrikaCapacityKg)

        val bottleneck = BottleneckAnalysis(
            stage = "SETRIKA",
            reason = "Kapasitas meja setrika terbatas (${setrikaCapacityKg.roundToInt()} kg/hari), sedangkan beban antrian mencapai ${setrikaLoadKg.roundToInt()} kg.",
            capacityKg = setrikaCapacityKg,
            currentLoadKg = setrikaLoadKg,
            utilizationPercent = setrikaUtil,
            estimatedBacklogKg = setrikaBacklog,
            recommendation = "Alihkan 1 staf cuci/packing untuk membantu proses setrika uap, atau atur antrian express lebih dulu."
        )

        return PipelineSummary(
            masukKg = masukKg,
            cuciKg = cuciKg,
            keringKg = keringKg,
            setrikaKg = setrikaKg,
            packingKg = packingKg,
            selesaiKg = selesaiKg,
            bottleneck = bottleneck
        )
    }

    fun computeUnitEconomics(config: BusinessConfig, volumeKgPerDay: Double = config.targetKgPerDay): UnitEconomics {
        val monthlyVolumeKg = (volumeKgPerDay * 30.0).coerceAtLeast(100.0)
        val revenuePerKg = config.regularPricePerKg
        val variableCostPerKg = config.variableCostPerKg
        val contributionMarginPerKg = revenuePerKg - variableCostPerKg
        val fixedCostAllocationPerKg = config.totalPureFixedCostMonthly / monthlyVolumeKg
        val netProfitPerKg = contributionMarginPerKg - fixedCostAllocationPerKg
        val marginPercent = if (revenuePerKg > 0) (netProfitPerKg / revenuePerKg) * 100.0 else 0.0

        return UnitEconomics(
            revenuePerKg = revenuePerKg,
            variableCostPerKg = variableCostPerKg,
            contributionMarginPerKg = contributionMarginPerKg,
            fixedCostAllocationPerKg = fixedCostAllocationPerKg,
            netProfitPerKg = netProfitPerKg,
            marginPercent = marginPercent
        )
    }

    fun computeBEP(config: BusinessConfig, actualDailyKg: Double = config.targetKgPerDay): BEPResult {
        val fixedCostMonthly = config.totalPureFixedCostMonthly
        val contributionMarginPerKg = (config.regularPricePerKg - config.variableCostPerKg).coerceAtLeast(500.0)
        val bepKgMonthly = fixedCostMonthly / contributionMarginPerKg
        val bepKgDaily = bepKgMonthly / 30.0
        val bepOmzet = bepKgMonthly * config.regularPricePerKg
        val gap = actualDailyKg - bepKgDaily

        return BEPResult(
            monthlyFixedCost = fixedCostMonthly,
            contributionMarginPerKg = contributionMarginPerKg,
            bepKgPerMonth = bepKgMonthly,
            bepKgPerDay = bepKgDaily,
            bepOmzetMonthly = bepOmzet,
            actualKgPerDay = actualDailyKg,
            gapKgPerDay = gap,
            isProfitable = gap >= 0
        )
    }

    fun computeSimulator(state: CockpitUiState): SimulatorCalculations {
        val kgDay = state.simOrderKgPerDay
        val priceKg = state.simPricePerKg
        val staffCount = state.simStaffCount
        val washCount = state.simWashingMachines
        val dryCount = state.simDryers
        val rent = state.simRentMonthly
        val electricity = state.simElectricityMonthly
        val salaryPerStaff = state.simSalaryPerStaff

        val baseConfig = state.config

        // Dynamic practical capacity with slider values
        val washCycles = (baseConfig.operatingHoursPerDay * 60.0) / baseConfig.washingCycleDurationMinutes
        val washCap = washCount * baseConfig.washingCapacityPerMachineKg * washCycles * baseConfig.operatingEfficiency

        val dryCycles = (baseConfig.operatingHoursPerDay * 60.0) / baseConfig.dryerCycleDurationMinutes
        val dryCap = dryCount * baseConfig.dryerCapacityPerMachineKg * dryCycles * baseConfig.operatingEfficiency

        val ironCap = (staffCount * 0.4).coerceAtLeast(1.0) * baseConfig.ironCapacityPerUnitPerHourKg * baseConfig.operatingHoursPerDay * baseConfig.operatingEfficiency
        val practicalMaxCapacityKg = minOf(washCap, dryCap, ironCap)

        val dailyRevenue = kgDay * priceKg
        val monthlyRevenue = dailyRevenue * 30.0

        val variableCostPerKg = baseConfig.chemicalPerKg + baseConfig.plasticPerKg + baseConfig.gasAndOtherPerKg +
                ((electricity + baseConfig.waterMonthlyBase) / (kgDay * 30.0).coerceAtLeast(100.0))

        val dailyVariableCost = variableCostPerKg * kgDay
        val monthlyFixedCost = rent + (staffCount * salaryPerStaff) + baseConfig.internetSoftwareMonthly + baseConfig.depreciationMonthly + baseConfig.adminOtherMonthly

        val totalMonthlyCost = (dailyVariableCost * 30.0) + monthlyFixedCost
        val monthlyProfit = monthlyRevenue - totalMonthlyCost
        val dailyProfit = monthlyProfit / 30.0
        val marginPercent = if (monthlyRevenue > 0) (monthlyProfit / monthlyRevenue) * 100.0 else 0.0

        val capacityUtilization = if (practicalMaxCapacityKg > 0) (kgDay / practicalMaxCapacityKg) * 100.0 else 0.0
        val isOverCapacity = capacityUtilization > 100.0

        val contributionMarginPerKg = (priceKg - variableCostPerKg).coerceAtLeast(100.0)
        val bepMonthly = monthlyFixedCost / contributionMarginPerKg
        val bepDaily = bepMonthly / 30.0

        val annualROI = if (baseConfig.initialCash > 0) ((monthlyProfit * 12.0) / baseConfig.initialCash) * 100.0 else 0.0

        return SimulatorCalculations(
            dailyRevenue = dailyRevenue,
            monthlyRevenue = monthlyRevenue,
            dailyVariableCost = dailyVariableCost,
            monthlyFixedCost = monthlyFixedCost,
            totalMonthlyCost = totalMonthlyCost,
            dailyProfit = dailyProfit,
            monthlyProfit = monthlyProfit,
            marginPercent = marginPercent,
            capacityUtilizationPercent = capacityUtilization,
            maxDailyPracticalCapacityKg = practicalMaxCapacityKg,
            bepKgPerDay = bepDaily,
            bepKgPerMonth = bepMonthly,
            monthlyCashflow = monthlyProfit,
            annualROI = annualROI,
            isOverCapacity = isOverCapacity
        )
    }

    fun computeMachineInvestment(config: BusinessConfig, input: MachineInvestmentInput): MachineInvestmentResult {
        // Extra capacity per day
        val cycles = (config.operatingHoursPerDay * 60.0) / input.cycleMinutes
        val extraPracticalDailyKg = input.capacityKg * cycles * config.operatingEfficiency

        // Potential extra revenue assuming 50% extra capacity utilized
        val utilizedKg = extraPracticalDailyKg * 0.5
        val extraMonthlyRevenue = utilizedKg * config.regularPricePerKg * 30.0
        val extraMonthlyCost = (utilizedKg * config.chemicalPerKg * 30.0) + input.additionalElectricityMonthly + input.additionalMaintenanceMonthly
        val extraMonthlyProfit = extraMonthlyRevenue - extraMonthlyCost

        val paybackMonths = if (extraMonthlyProfit > 0) input.machinePrice / extraMonthlyProfit else 999.0
        val annualROI = if (input.machinePrice > 0) ((extraMonthlyProfit * 12.0) / input.machinePrice) * 100.0 else 0.0

        val isRecommended = paybackMonths <= 18.0 && extraMonthlyProfit > 1_000_000.0
        val title = if (isRecommended) "LAYAK DIPERTIMBANGKAN" else "TIDAK PERLU BELI MESIN SAAT INI"
        val reason = if (isRecommended) {
            "Utilisasi mesin cuci sudah tinggi dan estimasi payback period cepat (~${String.format("%.1f", paybackMonths)} bulan)."
        } else {
            "Bottleneck utama saat ini adalah proses SETRIKA, bukan mesin cuci. Menambah mesin cuci belum akan meningkatkan omzet secara signifikan."
        }

        return MachineInvestmentResult(
            additionalDailyCapacityKg = extraPracticalDailyKg,
            additionalMonthlyRevenuePotential = extraMonthlyRevenue,
            additionalMonthlyNetProfit = extraMonthlyProfit,
            paybackPeriodMonths = paybackMonths,
            annualROI = annualROI,
            recommendationTitle = title,
            recommendationReason = reason,
            isRecommended = isRecommended
        )
    }

    fun computeCashflowForecast(config: BusinessConfig, dailyProfit: Double): List<CashflowForecastItem> {
        val currentCash = config.initialCash
        val periods = listOf(
            Pair("7 Hari", 7),
            Pair("30 Hari (1 Bulan)", 30),
            Pair("3 Bulan", 90),
            Pair("6 Bulan", 180),
            Pair("12 Bulan (1 Tahun)", 365)
        )

        return periods.map { (label, days) ->
            val rev = (config.targetKgPerDay * config.regularPricePerKg) * days
            val dailyExp = (config.totalPureFixedCostMonthly / 30.0) + (config.variableCostPerKg * config.targetKgPerDay)
            val exp = dailyExp * days
            val net = rev - exp
            val endCash = currentCash + net

            CashflowForecastItem(
                periodLabel = label,
                days = days,
                projectedRevenue = rev,
                projectedExpenses = exp,
                projectedNetCash = net,
                endingCashBalance = endCash
            )
        }
    }

    fun computeCustomerEconomics(ordersCount: Int, regularPrice: Double): CustomerEconomics {
        val activeCust = 142
        val avgKg = 5.6
        val avgTrx = avgKg * regularPrice
        val freq = 2.8 // times a month
        val revPerCustMonth = avgTrx * freq
        val clv = revPerCustMonth * 12.0 // 1 year simple CLV
        return CustomerEconomics(
            activeCustomers = activeCust,
            avgTransactionRp = avgTrx,
            avgKgPerCustomer = avgKg,
            monthlyFrequency = freq,
            revenuePerCustomerMonthly = revPerCustMonth,
            simpleCLV = clv,
            repeatCustomerRate = 76.5,
            newCustomersCount = 28,
            lostCustomersCount = 5
        )
    }
}
