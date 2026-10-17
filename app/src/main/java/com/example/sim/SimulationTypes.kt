package com.example.sim

import java.text.NumberFormat
import java.util.Locale

// Rupiah & metric formatting
fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount.toLong())}"
}

fun formatMoneyCompact(amount: Double): String {
    val absVal = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        absVal >= 1_000_000_000 -> "$sign${String.format(Locale("id", "ID"), "%.1f", absVal / 1_000_000_000)} M"
        absVal >= 1_000_000 -> "$sign${String.format(Locale("id", "ID"), "%.1f", absVal / 1_000_000)} jt"
        absVal >= 1_000 -> "$sign${String.format(Locale("id", "ID"), "%.0f", absVal / 1_000)} rb"
        else -> "$sign${amount.toLong()}"
    }
}

enum class SimSpeed(val label: String, val multiplier: Int) {
    PAUSED("Jeda", 0),
    SPEED_1X("1x", 1),
    SPEED_2X("2x", 3),
    SPEED_5X("5x", 8)
}

enum class Weather(val label: String, val icon: String, val dryTimeMultiplier: Double) {
    CERAH("Cerah", "☀️", 1.0),
    MENDUNG("Mendung", "⛅", 2.0),
    HUJAN("Hujan Deras", "🌧️", 4.5)
}

enum class StationPhase(val step: Int, val title: String, val icon: String, val colorHex: Long) {
    MASUK(1, "Masuk", "🧺", 0xFF10B981),
    CUCI(2, "Cuci", "🫧", 0xFF0284C7),
    JEMUR_KERING(3, "Kering", "☀️", 0xFFF59E0B),
    SETRIKA(4, "Setrika", "💨", 0xFFEF4444),
    PACKING(5, "Packing", "📦", 0xFF8B5CF6),
    SELESAI(6, "Selesai", "✨", 0xFF0D9488)
}

enum class AssetType(val displayName: String, val defaultCapacityKg: Double, val defaultCycleMinutes: Int, val purchaseCost: Double, val icon: String) {
    MESIN_CUCI_7KG("Mesin Cuci 7 kg", 7.0, 45, 3_800_000.0, "🫧"),
    MESIN_CUCI_14KG("Mesin Cuci Komersial 14 kg", 14.0, 40, 8_500_000.0, "⚡"),
    JEMURAN_TALI("Rak / Tali Jemuran (Outdoors)", 20.0, 180, 500_000.0, "👕"),
    DRYER_GAS_10KG("Dryer Gas Komersial 10 kg", 10.0, 45, 9_500_000.0, "🔥"),
    SETRIKA_LISTRIK("Meja Setrika Listrik", 6.0, 60, 450_000.0, "🔌"),
    SETRIKA_UAP_BOILER("Meja Setrika Uap Boiler", 15.0, 60, 3_200_000.0, "💨"),
    MEJA_PACKING("Meja Lipat & Packing", 30.0, 30, 750_000.0, "📦")
}

data class EquipmentItem(
    val id: String,
    val type: AssetType,
    val capacityKg: Double = type.defaultCapacityKg,
    val cycleMinutes: Int = type.defaultCycleMinutes,
    var isRunning: Boolean = false,
    var remainingMinutesInCycle: Int = 0,
    var currentBatchWeightKg: Double = 0.0,
    var healthPercent: Int = 100,
    var level: Int = 1
)

data class WorkerStaff(
    val id: String,
    val name: String,
    val roleTitle: String = "Operator",
    val avatarColor: Long,
    var assignedPhase: StationPhase = StationPhase.CUCI,
    var energyPercent: Int = 100, // Kelelahan
    var isOvertime: Boolean = false,
    var moodEmoji: String = "😊",
    val speedMultiplier: Double = 1.0,
    val dailySalary: Double = 90_000.0,
    var level: Int = 1
)

data class SimOrder(
    val id: String,
    val customerName: String,
    val weightKg: Double,
    val isExpress: Boolean,
    val priceTotal: Double,
    var currentPhase: StationPhase = StationPhase.MASUK,
    var minutesInCurrentPhase: Int = 0,
    var requiredMinutesInPhase: Int = 30,
    val arrivalDay: Int,
    val arrivalMinuteOfDay: Int,
    val deadlineDay: Int,
    val deadlineMinuteOfDay: Int,
    var isCompleted: Boolean = false,
    var isDelivered: Boolean = false,
    var isPaid: Boolean = false,
    var isLate: Boolean = false,
    var hadRework: Boolean = false
)

enum class GrowthTier(val title: String, val levelNumber: Int, val maxDailyCapacityTargetKg: Double, val requiredXp: Int) {
    RUMAHAN("Garasi Rumahan", 1, 20.0, 0),
    BERKEMBANG("Laundry Berkembang", 2, 40.0, 150),
    OUTLET_KECIL("Outlet Ruko Modern", 3, 80.0, 450),
    OUTLET_BERTUMBUH("Megastore Unggulan", 4, 120.0, 1000),
    INDUSTRI("Sentra Laundry Industri", 5, 200.0, 2200)
}

data class GameQuest(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double,
    var currentProgress: Double,
    val rewardMoney: Double,
    val rewardXp: Int,
    var isClaimed: Boolean = false
)

data class FloatingParticle(
    val id: String,
    val text: String,
    val colorHex: Long,
    val xRatio: Float,
    val yRatio: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class TaskCompletionFeedback(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val xpEarned: Int,
    val cashEarned: Double = 0.0,
    val comboCount: Int = 1,
    val phase: StationPhase,
    val timestamp: Long = System.currentTimeMillis()
)

data class StreakCelebration(
    val streakDays: Int,
    val bonusCash: Double,
    val bonusXp: Int,
    val title: String,
    val multiplierPercent: Int
)

data class DailySummaryReport(
    val day: Int,
    val totalOrdersReceived: Int,
    val totalKgReceived: Double,
    val totalOrdersCompleted: Int,
    val totalKgCompleted: Double,
    val onTimePercentage: Double,
    val revenueToday: Double,
    val operatingExpensesToday: Double,
    val netProfitToday: Double,
    val endingCash: Double,
    val bottleneckPhase: StationPhase,
    val keyInsight: String,
    val leftoverBacklogKg: Double,
    val efficiencyStreak: Int = 0,
    val streakBonusCash: Double = 0.0,
    val isStreakMaintained: Boolean = false,
    val zeroLateStreak: Int = 0
)

data class ZeroLateStreakInfo(
    val currentStreakDays: Int,
    val highestStreakDays: Int,
    val todayLateOrders: Int,
    val todayCompletedOrders: Int,
    val todayOnTimePercentage: Double,
    val visualSatisfactionBonusMultiplier: Double,
    val bonusCashAccumulated: Double,
    val tierTitle: String
)

data class LevelUpCelebration(
    val oldLevelNumber: Int,
    val newLevelNumber: Int,
    val oldTierTitle: String,
    val newTierTitle: String,
    val oldCapacityKg: Double,
    val newCapacityKg: Double,
    val bonusCash: Double,
    val unlockedPerks: List<String>
)

data class BottleneckDiagnosis(
    val phase: StationPhase,
    val loadKg: Double,
    val waitMinutes: Int,
    val cause: String,
    val suggestion: String
)
