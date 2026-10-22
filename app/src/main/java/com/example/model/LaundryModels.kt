package com.example.model

import java.text.NumberFormat
import java.util.Locale

// Rupiah & Number Formatting Helper
fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount.toLong())}"
}

fun formatRupiahCompact(amount: Double): String {
    val absVal = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        absVal >= 1_000_000_000 -> "$sign${String.format(Locale("id", "ID"), "%.1f", absVal / 1_000_000_000)} M"
        absVal >= 1_000_000 -> "$sign${String.format(Locale("id", "ID"), "%.1f", absVal / 1_000_000)} jt"
        absVal >= 1_000 -> "$sign${String.format(Locale("id", "ID"), "%.0f", absVal / 1_000)} rb"
        else -> "$sign${kotlin.math.abs(amount).toLong()}"
    }
}

fun formatKg(kg: Double): String {
    return if (kg % 1.0 == 0.0) "${kg.toInt()} kg" else String.format(Locale("id", "ID"), "%.1f kg", kg)
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    MASUK("Masuk", 0),
    CUCI("Cuci", 1),
    KERING("Kering", 2),
    SETRIKA("Setrika", 3),
    PACKING("Packing", 4),
    SELESAI("Selesai", 5),
    DIAMBIL("Diambil", 6);

    fun nextStatus(): OrderStatus? {
        return when (this) {
            MASUK -> CUCI
            CUCI -> KERING
            KERING -> SETRIKA
            SETRIKA -> PACKING
            PACKING -> SELESAI
            SELESAI -> DIAMBIL
            DIAMBIL -> null
        }
    }
}

enum class ServiceType(val displayName: String, val defaultPricePerKg: Double, val isPerPiece: Boolean = false) {
    REGULER("Laundry Reguler", 9_000.0),
    EXPRESS("Laundry Express", 15_000.0),
    CUCI_SETRIKA("Cuci + Setrika", 9_000.0),
    CUCI_SAJA("Cuci Saja", 6_000.0),
    SETRIKA_SAJA("Setrika Saja", 6_000.0),
    BEDCOVER("Bedcover", 25_000.0, true),
    SEPATU("Sepatu", 30_000.0, true),
    KARPET("Karpet", 20_000.0, true),
    LAINNYA("Layanan Lain", 10_000.0)
}

data class LaundryOrder(
    val id: String,
    val customerName: String,
    val phone: String = "",
    val serviceType: ServiceType = ServiceType.REGULER,
    val weightKg: Double,
    val pricePerKg: Double,
    val totalPrice: Double = weightKg * pricePerKg,
    val deadline: String = "Hari ini", // Hari ini, Besok, 2 hari, 3 hari
    val status: OrderStatus = OrderStatus.MASUK,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class BusinessConfig(
    val businessName: String = "Laundry Bersih",
    val regularPricePerKg: Double = 9_000.0,
    val expressPricePerKg: Double = 15_000.0,
    val targetKgPerDay: Double = 120.0,
    val operatingHoursPerDay: Double = 12.0,
    val operatingEfficiency: Double = 0.80, // 80% practical factor

    // Mesin Cuci
    val washingMachineCount: Int = 3,
    val washingCapacityPerMachineKg: Double = 10.0,
    val washingCycleDurationMinutes: Double = 45.0,

    // Dryer
    val dryerCount: Int = 2,
    val dryerCapacityPerMachineKg: Double = 10.0,
    val dryerCycleDurationMinutes: Double = 45.0,

    // Setrika
    val ironCount: Int = 2,
    val ironCapacityPerUnitPerHourKg: Double = 10.0,

    // Karyawan
    val staffCount: Int = 4,
    val salaryPerStaffMonthly: Double = 3_000_000.0,

    // Fixed Costs (Monthly)
    val rentMonthly: Double = 6_000_000.0,
    val internetSoftwareMonthly: Double = 500_000.0,
    val depreciationMonthly: Double = 1_000_000.0,
    val adminOtherMonthly: Double = 500_000.0,

    // Variable Costs
    val electricityMonthlyBase: Double = 3_500_000.0,
    val waterMonthlyBase: Double = 1_500_000.0,
    val chemicalPerKg: Double = 1_200.0, // Detergen, softener, parfum
    val plasticPerKg: Double = 600.0,
    val gasAndOtherPerKg: Double = 800.0,

    // Initial Working Capital / Cash
    val initialCash: Double = 25_000_000.0
) {
    val safeOperatingHours: Double get() = operatingHoursPerDay.coerceIn(1.0, 24.0)
    val safeEfficiency: Double get() = operatingEfficiency.coerceIn(0.10, 1.0)

    val totalStaffCostMonthly: Double get() = staffCount.coerceAtLeast(0) * salaryPerStaffMonthly.coerceAtLeast(0.0)
    val totalPureFixedCostMonthly: Double get() = rentMonthly + totalStaffCostMonthly + internetSoftwareMonthly + depreciationMonthly + adminOtherMonthly

    // Variable cost breakdown per kg (chemical + plastic + gas + variable utilities)
    val variableCostPerKg: Double get() = chemicalPerKg + plasticPerKg + gasAndOtherPerKg +
            ((electricityMonthlyBase + waterMonthlyBase) / (targetKgPerDay * 30.0).coerceAtLeast(100.0))

    // Theoretical & Practical Capacities (guarded against division by zero)
    val washingCyclesPerDay: Double get() = (safeOperatingHours * 60.0) / washingCycleDurationMinutes.coerceAtLeast(1.0)
    val washingTheoreticalCapacityKg: Double get() = washingMachineCount.coerceAtLeast(0) * washingCapacityPerMachineKg.coerceAtLeast(0.0) * washingCyclesPerDay
    val washingPracticalCapacityKg: Double get() = washingTheoreticalCapacityKg * safeEfficiency

    val dryerCyclesPerDay: Double get() = (safeOperatingHours * 60.0) / dryerCycleDurationMinutes.coerceAtLeast(1.0)
    val dryerTheoreticalCapacityKg: Double get() = dryerCount.coerceAtLeast(0) * dryerCapacityPerMachineKg.coerceAtLeast(0.0) * dryerCyclesPerDay
    val dryerPracticalCapacityKg: Double get() = dryerTheoreticalCapacityKg * safeEfficiency

    val ironTheoreticalCapacityKg: Double get() = ironCount.coerceAtLeast(0) * ironCapacityPerUnitPerHourKg.coerceAtLeast(0.0) * safeOperatingHours
    val ironPracticalCapacityKg: Double get() = ironTheoreticalCapacityKg * safeEfficiency

    // Overall max capacity is limited by the bottleneck stage
    val maxDailyPracticalCapacityKg: Double get() = minOf(
        washingPracticalCapacityKg.coerceAtLeast(1.0),
        if (dryerCount > 0) dryerPracticalCapacityKg.coerceAtLeast(1.0) else washingPracticalCapacityKg.coerceAtLeast(1.0),
        ironPracticalCapacityKg.coerceAtLeast(1.0)
    )
}

enum class HealthStatus {
    GREEN, YELLOW, RED
}

data class HealthIndicator(
    val title: String,
    val status: HealthStatus,
    val detail: String
)

data class BottleneckAnalysis(
    val stage: String,
    val reason: String,
    val capacityKg: Double,
    val currentLoadKg: Double,
    val utilizationPercent: Double,
    val estimatedBacklogKg: Double,
    val recommendation: String
)

data class UnitEconomics(
    val revenuePerKg: Double,
    val variableCostPerKg: Double,
    val contributionMarginPerKg: Double,
    val fixedCostAllocationPerKg: Double,
    val netProfitPerKg: Double,
    val marginPercent: Double
)

data class BEPResult(
    val monthlyFixedCost: Double,
    val contributionMarginPerKg: Double,
    val bepKgPerMonth: Double,
    val bepKgPerDay: Double,
    val bepOmzetMonthly: Double,
    val actualKgPerDay: Double,
    val gapKgPerDay: Double,
    val isProfitable: Boolean
)

data class MachineInvestmentInput(
    val machinePrice: Double = 15_000_000.0,
    val machineType: String = "Mesin Cuci",
    val capacityKg: Double = 15.0,
    val cycleMinutes: Double = 45.0,
    val additionalElectricityMonthly: Double = 350_000.0,
    val additionalMaintenanceMonthly: Double = 200_000.0
)

data class MachineInvestmentResult(
    val additionalDailyCapacityKg: Double,
    val additionalMonthlyRevenuePotential: Double,
    val additionalMonthlyNetProfit: Double,
    val paybackPeriodMonths: Double,
    val annualROI: Double,
    val recommendationTitle: String,
    val recommendationReason: String,
    val isRecommended: Boolean
)

enum class ScenarioPreset(val label: String, val description: String) {
    NORMAL("Normal", "Kondisi saat ini"),
    ORDER_UP_20("Order +20%", "Kenaikan permintaan harian 20%"),
    ORDER_UP_50("Order +50%", "Lonjakan drastis pesanan 50%"),
    ORDER_DOWN_20("Order -20%", "Penurunan sepi order 20%"),
    PRICE_UP_10("Harga +10%", "Kenaikan tarif cuci 10%"),
    SALARY_UP_10("Gaji +10%", "Kenaikan UMR/gaji karyawan 10%"),
    ELECTRICITY_UP_20("Listrik +20%", "Kenaikan TDL tarif listrik 20%"),
    MACHINE_BROKEN("Mesin Rusak", "1 Mesin cuci down rusak"),
    ADD_MACHINE("Tambah 1 Mesin", "Investasi 1 mesin cuci baru"),
    ADD_STAFF("Tambah 1 Karyawan", "Rekrut 1 tenaga setrika/operasional"),
    EXPRESS_SURGE("Express Meningkat", "Porsi order express naik jadi 40%")
}
