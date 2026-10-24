package com.example.sim

import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

class SimulationEngine(
    var isLabMode: Boolean = false
) {
    // Current Calendar & Clock
    var currentDay: Int = 1
    var currentMinuteOfDay: Int = 480 // 08:00 AM (8 * 60)
    val startWorkMinute: Int = 480 // 08:00
    val normalCloseWorkMinute: Int = 1080 // 18:00
    val overtimeCloseWorkMinute: Int = 1200 // 20:00

    var simSpeed: SimSpeed = SimSpeed.SPEED_1X
    var weatherToday: Weather = Weather.CERAH
    var growthTier: GrowthTier = GrowthTier.RUMAHAN

    // Tycoon XP & Level Progress
    var currentXp: Int = 30
    var tycoonLevel: Int = 1
    var currentStreak: Int = 1
    var highestStreak: Int = 1
    var zeroLateStreakDays: Int = 2
    var highestZeroLateStreakDays: Int = 2
    var lateOrdersCountToday: Int = 0
    var satisfactionBonusClaimedToday: Boolean = false
    var taskComboCount: Int = 0
    var lastTaskCompletedTimestamp: Long = 0L
    var latestCompletionFeedback: TaskCompletionFeedback? = null
    var pendingLevelUp: LevelUpCelebration? = null
    var pendingStreakCelebration: StreakCelebration? = null

    // Reactive 3D Station Tap Tracking (for 3D bounce & shockwave ring)
    var lastInteractedPhase: StationPhase? = null
    var lastInteractionTimestamp: Long = 0L

    // Business Balance & Reputation
    var cash: Double = 5_000_000.0 // Rp 5 juta modal pengembangan awal
    var reputationStars: Double = 4.7 // out of 5.0
    var repeatCustomerRatePercent: Double = 72.0

    // Today's planning policies
    var maxDailyIntakeKgQuota: Double = 25.0
    var isAcceptingNewOrdersToday: Boolean = true
    var isOvertimeActiveToday: Boolean = false

    // Operational Resources & Team
    val equipmentList = mutableListOf<EquipmentItem>()
    val workerList = mutableListOf<WorkerStaff>()
    val ordersList = mutableListOf<SimOrder>()

    // Financial logs today
    var revenueCollectedToday: Double = 0.0
    var operationalExpensesToday: Double = 0.0
    var completedOrdersCountToday: Int = 0
    var completedKgToday: Double = 0.0
    var reworkCountToday: Int = 0

    // Game Floating Particles Queue
    val floatingParticles = mutableListOf<FloatingParticle>()

    // Game Quests
    val activeQuests = mutableListOf<GameQuest>()

    // History logs
    val historyReports = mutableListOf<DailySummaryReport>()

    init {
        setupDefaultRumahanState()
        refreshDailyQuests()
    }

    fun setupDefaultRumahanState() {
        equipmentList.clear()
        workerList.clear()
        ordersList.clear()

        // 1 Mesin Cuci 7kg, 1 Jemuran Tali, 1 Meja Setrika Listrik, 1 Meja Packing
        equipmentList.add(EquipmentItem("EQ-W1", AssetType.MESIN_CUCI_7KG))
        equipmentList.add(EquipmentItem("EQ-J1", AssetType.JEMURAN_TALI, capacityKg = 25.0))
        equipmentList.add(EquipmentItem("EQ-S1", AssetType.SETRIKA_LISTRIK))
        equipmentList.add(EquipmentItem("EQ-P1", AssetType.MEJA_PACKING))

        // 2 Workers: Budi (Pemilik) and Siti (Asisten)
        workerList.add(WorkerStaff("W-1", "Budi (Pemilik)", "Kepala Cuci", 0xFF0284C7, assignedPhase = StationPhase.CUCI))
        workerList.add(WorkerStaff("W-2", "Siti (Asisten)", "Ahli Setrika", 0xFF0D9488, assignedPhase = StationPhase.SETRIKA))

        // Initial backlog orders
        ordersList.add(
            SimOrder(
                id = "ORD-101",
                customerName = "Bu Ratna (Langganan)",
                weightKg = 5.0,
                isExpress = false,
                priceTotal = 45_000.0,
                currentPhase = StationPhase.MASUK,
                arrivalDay = 1,
                arrivalMinuteOfDay = 485,
                deadlineDay = 1,
                deadlineMinuteOfDay = 1020
            )
        )
        ordersList.add(
            SimOrder(
                id = "ORD-102",
                customerName = "Pak Hendra (Kos)",
                weightKg = 4.5,
                isExpress = true,
                priceTotal = 67_500.0,
                currentPhase = StationPhase.CUCI,
                arrivalDay = 1,
                arrivalMinuteOfDay = 490,
                deadlineDay = 1,
                deadlineMinuteOfDay = 960
            )
        )
        ordersList.add(
            SimOrder(
                id = "ORD-103",
                customerName = "Mbak Dewi (Guru)",
                weightKg = 6.0,
                isExpress = false,
                priceTotal = 54_000.0,
                currentPhase = StationPhase.JEMUR_KERING,
                arrivalDay = 1,
                arrivalMinuteOfDay = 495,
                deadlineDay = 2,
                deadlineMinuteOfDay = 720
            )
        )
    }

    fun refreshDailyQuests() {
        activeQuests.clear()
        activeQuests.add(
            GameQuest("Q-1", "Selesaikan 15 kg Cucian", "Cuci, keringkan & packing 15 kg hari ini", 15.0, 0.0, 150_000.0, 45)
        )
        activeQuests.add(
            GameQuest("Q-2", "Beri Kopi Booster Staf", "Segarkan pekerja agar energi kembali 100%", 1.0, 0.0, 75_000.0, 30)
        )
        activeQuests.add(
            GameQuest("Q-3", "Tanpa Keterlambatan SLA", "Pertahankan reputasi bintang di atas 4.5★", 4.5, reputationStars, 200_000.0, 60)
        )
    }

    // Time string helper (e.g. "09:45")
    fun getFormattedTime(): String {
        val hours = currentMinuteOfDay / 60
        val mins = currentMinuteOfDay % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins)
    }

    fun isWorkHours(): Boolean {
        val closeMinute = if (isOvertimeActiveToday) overtimeCloseWorkMinute else normalCloseWorkMinute
        return currentMinuteOfDay in startWorkMinute..closeMinute
    }

    /**
     * Returns a 0f..1f progress fraction for the active batch at a given station phase,
     * used to render live 3D progress bars and active station animations.
     */
    fun getPhaseProgressFraction(phase: StationPhase): Float {
        val order = ordersList.firstOrNull { it.currentPhase == phase && !it.isDelivered } ?: return 0f
        val hasGasDryer = equipmentList.any { it.type == AssetType.DRYER_GAS_10KG }
        val hasBoiler = equipmentList.any { it.type == AssetType.SETRIKA_UAP_BOILER }
        val req = when (phase) {
            StationPhase.MASUK -> 12f
            StationPhase.CUCI -> order.requiredMinutesInPhase.coerceIn(20, 45).toFloat()
            StationPhase.JEMUR_KERING -> (if (hasGasDryer) 45f else (180f * weatherToday.dryTimeMultiplier.toFloat()))
            StationPhase.SETRIKA -> (order.weightKg * (if (hasBoiler) 5.0 else 9.0)).toFloat().coerceAtLeast(15f)
            StationPhase.PACKING -> (order.weightKg * 3.0).toFloat().coerceAtLeast(10f)
            StationPhase.SELESAI -> 1f
        }
        return (order.minutesInCurrentPhase.toFloat() / req.coerceAtLeast(1f)).coerceIn(0.08f, 1f)
    }

    // Advance clock by delta minutes
    fun tickMinutes(minutesToAdvance: Int = 1) {
        if (simSpeed == SimSpeed.PAUSED || minutesToAdvance <= 0) return

        val closeMinute = if (isOvertimeActiveToday) overtimeCloseWorkMinute else normalCloseWorkMinute

        for (m in 1..minutesToAdvance) {
            currentMinuteOfDay++

            if (currentMinuteOfDay > closeMinute) {
                simSpeed = SimSpeed.PAUSED
                break
            }

            // Customer Arrival Logic
            if (isWorkHours() && isAcceptingNewOrdersToday) {
                maybeGenerateIncomingCustomer()
            }

            // Process Operations
            processOperationsTick()
        }

        // Clean up old floating particles (older than 2.5s)
        val now = System.currentTimeMillis()
        floatingParticles.removeAll { now - it.timestamp > 2500 }
    }

    private fun maybeGenerateIncomingCustomer() {
        val currentKgToday = ordersList.filter { it.arrivalDay == currentDay }.sumOf { it.weightKg }
        if (currentKgToday >= maxDailyIntakeKgQuota) return

        val isCustomerWindow = currentMinuteOfDay in 510..940
        if (isCustomerWindow && currentMinuteOfDay % 70 == 0) {
            val randomRoll = Random.nextDouble()
            if (randomRoll < (reputationStars / 5.0) * 0.88) {
                spawnCustomerOrder()
            }
        }
    }

    fun spawnCustomerOrder() {
        val orderNum = ordersList.size + 1
        val isExp = Random.nextDouble() < 0.30
        val weight = (Random.nextDouble(2.5, 7.5) * 10).roundToInt() / 10.0
        val pricePerKg = if (isExp) 15_000.0 else 9_000.0
        val totalPrice = weight * pricePerKg

        val customerNames = listOf("Ibu Maya", "Mas Arif", "Bu Wahyu", "Pak Joko", "Mba Lia", "Dokter Dini", "Kak Kevin", "Bu Citra")
        val chosenName = customerNames[orderNum % customerNames.size]

        val deadlineMins = if (isExp) 1020 else 1080
        val deadlineDay = if (isExp || currentMinuteOfDay < 660) currentDay else currentDay + 1

        val newOrder = SimOrder(
            id = "ORD-${String.format("%03d", orderNum)}",
            customerName = chosenName,
            weightKg = weight,
            isExpress = isExp,
            priceTotal = totalPrice,
            currentPhase = StationPhase.MASUK,
            arrivalDay = currentDay,
            arrivalMinuteOfDay = currentMinuteOfDay,
            deadlineDay = deadlineDay,
            deadlineMinuteOfDay = deadlineMins
        )
        ordersList.add(newOrder)

        // Spawn floating text
        addParticle("🧺 Order Baru! ${weight}kg", 0xFF10B981, 0.24f, 0.65f)
    }

    private fun processOperationsTick() {
        val inWorkTime = isWorkHours()

        // 1. MASUK
        val hasSortWorker = workerList.any { it.assignedPhase == StationPhase.MASUK || it.assignedPhase == StationPhase.CUCI }
        val masukOrders = ordersList.filter { it.currentPhase == StationPhase.MASUK }
        for (order in masukOrders) {
            order.minutesInCurrentPhase++
            val requiredMins = if (hasSortWorker) 12 else 35
            if (order.minutesInCurrentPhase >= requiredMins) {
                order.currentPhase = StationPhase.CUCI
                order.minutesInCurrentPhase = 0
            }
        }

        // 2. CUCI
        val cuciOrders = ordersList.filter { it.currentPhase == StationPhase.CUCI }
        val availableWashers = equipmentList.filter {
            (it.type == AssetType.MESIN_CUCI_7KG || it.type == AssetType.MESIN_CUCI_14KG) && it.healthPercent > 20
        }
        val cuciWorkers = workerList.filter { it.assignedPhase == StationPhase.CUCI }

        for (washer in availableWashers) {
            if (washer.isRunning) {
                washer.remainingMinutesInCycle--
                if (washer.remainingMinutesInCycle <= 0) {
                    washer.isRunning = false
                    washer.currentBatchWeightKg = 0.0
                    operationalExpensesToday += 2_500.0
                    onTaskCompleted("🫧 Cuci Selesai!", "Batch siap dikeringkan", 12, phase = StationPhase.CUCI)
                }
            }
        }

        if (cuciWorkers.isNotEmpty() && inWorkTime) {
            for (washer in availableWashers) {
                if (!washer.isRunning) {
                    val nextBatch = cuciOrders.filter { it.minutesInCurrentPhase == 0 }.take(1)
                    if (nextBatch.isNotEmpty()) {
                        val batchOrder = nextBatch.first()
                        washer.isRunning = true
                        washer.remainingMinutesInCycle = washer.cycleMinutes
                        washer.currentBatchWeightKg = batchOrder.weightKg
                        batchOrder.requiredMinutesInPhase = washer.cycleMinutes
                        batchOrder.minutesInCurrentPhase = 1
                    }
                }
            }
        }

        for (order in cuciOrders) {
            if (order.minutesInCurrentPhase > 0) {
                order.minutesInCurrentPhase++
                val targetWashMins = order.requiredMinutesInPhase.coerceIn(20, 45)
                if (order.minutesInCurrentPhase >= targetWashMins) {
                    order.currentPhase = StationPhase.JEMUR_KERING
                    order.minutesInCurrentPhase = 0
                }
            }
        }

        // 3. JEMUR / KERING
        val keringOrders = ordersList.filter { it.currentPhase == StationPhase.JEMUR_KERING }
        val gasDryers = equipmentList.filter { it.type == AssetType.DRYER_GAS_10KG && it.healthPercent > 20 }
        val hasGasDryer = gasDryers.isNotEmpty()

        for (order in keringOrders) {
            order.minutesInCurrentPhase++
            val baseDryMinutes = if (hasGasDryer) 45 else (180 * weatherToday.dryTimeMultiplier).roundToInt()

            if (order.minutesInCurrentPhase >= baseDryMinutes) {
                order.currentPhase = StationPhase.SETRIKA
                order.minutesInCurrentPhase = 0
                if (hasGasDryer) {
                    operationalExpensesToday += 1_800.0
                }
            }
        }

        // 4. SETRIKA
        val setrikaOrders = ordersList.filter { it.currentPhase == StationPhase.SETRIKA }
        val setrikaWorkers = workerList.filter { it.assignedPhase == StationPhase.SETRIKA }
        val hasBoiler = equipmentList.any { it.type == AssetType.SETRIKA_UAP_BOILER }

        if (setrikaWorkers.isNotEmpty() && inWorkTime) {
            val speedFactor = (if (hasBoiler) 1.6 else 1.0) * setrikaWorkers.sumOf { it.speedMultiplier }
            for (order in setrikaOrders) {
                order.minutesInCurrentPhase += (1 * speedFactor).roundToInt().coerceAtLeast(1)

                val requiredIronMinutes = (order.weightKg * (if (hasBoiler) 5.0 else 9.0)).roundToInt()
                if (order.minutesInCurrentPhase >= requiredIronMinutes) {
                    val avgEnergy = setrikaWorkers.map { it.energyPercent }.average()
                    if (avgEnergy < 40 && Random.nextDouble() < 0.15) {
                        order.hadRework = true
                        reworkCountToday++
                        order.minutesInCurrentPhase = requiredIronMinutes / 2
                        addParticle("⚠️ Kusut, Rework!", 0xFFEF4444, 0.55f, 0.65f)
                    } else {
                        order.currentPhase = StationPhase.PACKING
                        order.minutesInCurrentPhase = 0
                        onTaskCompleted("💨 Licin Rapi!", "Siap di-packing rapi", 15, phase = StationPhase.SETRIKA)
                    }
                }
                break
            }
        }

        // 5. PACKING
        val packingOrders = ordersList.filter { it.currentPhase == StationPhase.PACKING }
        val packingWorkers = workerList.filter { it.assignedPhase == StationPhase.PACKING || it.assignedPhase == StationPhase.SETRIKA }
        if (packingWorkers.isNotEmpty() && inWorkTime) {
            for (order in packingOrders) {
                order.minutesInCurrentPhase++
                val requiredPackingMinutes = (order.weightKg * 3.0).roundToInt().coerceAtLeast(10)
                if (order.minutesInCurrentPhase >= requiredPackingMinutes) {
                    completeAndPayOrder(order)
                }
            }
        }

        // 6. SELESAI (Payment collected)
        val selesaiOrders = ordersList.filter { it.currentPhase == StationPhase.SELESAI && !it.isPaid }
        for (order in selesaiOrders) {
            order.isPaid = true
            order.isDelivered = true
            revenueCollectedToday += order.priceTotal
            cash += order.priceTotal
            onTaskCompleted("+${formatMoneyCompact(order.priceTotal)} 🪙", "Kas masuk dari ${order.customerName}", 15, cashEarned = order.priceTotal, phase = StationPhase.SELESAI)
        }

        // Check active in-progress orders passing deadline
        val totalCurrentMins = (currentDay * 1440) + currentMinuteOfDay
        for (ord in ordersList.filter { !it.isCompleted && !it.isLate }) {
            val dTotal = (ord.deadlineDay * 1440) + ord.deadlineMinuteOfDay
            if (totalCurrentMins > dTotal) {
                ord.isLate = true
                lateOrdersCountToday++
                addParticle("⚠️ Terlambat: ${ord.customerName}!", 0xFFEF4444, 0.5f, 0.65f)
            }
        }

        // Worker Fatigue decay & mood
        if (inWorkTime && currentMinuteOfDay % 25 == 0) {
            for (w in workerList) {
                val decay = if (w.isOvertime) 4 else 2
                w.energyPercent = (w.energyPercent - decay).coerceAtLeast(20)
                w.moodEmoji = when {
                    w.energyPercent > 70 -> "😊"
                    w.energyPercent > 40 -> "😐"
                    else -> "😴"
                }
            }
        }
    }

    private fun completeAndPayOrder(order: SimOrder) {
        order.currentPhase = StationPhase.SELESAI
        order.isCompleted = true
        order.minutesInCurrentPhase = 0
        completedOrdersCountToday++
        completedKgToday += order.weightKg
        operationalExpensesToday += (order.weightKg * 600.0)

        val currentTotalMinute = (currentDay * 1440) + currentMinuteOfDay
        val deadlineTotalMinute = (order.deadlineDay * 1440) + order.deadlineMinuteOfDay
        val isOnTime = currentTotalMinute <= deadlineTotalMinute

        if (isOnTime) {
            reputationStars = (reputationStars + 0.02).coerceAtMost(5.0)
            onTaskCompleted("✨ Selesai Tepat Waktu!", "${order.customerName} (${order.weightKg} kg)", 25, phase = StationPhase.PACKING)
            addParticle("🛡️ On-Time! Streak Aman", 0xFF10B981, 0.45f, 0.45f)
        } else {
            if (!order.isLate) {
                order.isLate = true
                lateOrdersCountToday++
            }
            reputationStars = (reputationStars - 0.05).coerceAtLeast(3.0)
            onTaskCompleted("⏰ Selesai Terlambat", "${order.customerName} lewat batas waktu", 10, phase = StationPhase.PACKING)
            addParticle("⚠️ Order Terlambat!", 0xFFEF4444, 0.5f, 0.6f)
        }

        // Collect payment immediately so player sees immediate reward
        if (!order.isPaid) {
            order.isPaid = true
            order.isDelivered = true
            revenueCollectedToday += order.priceTotal
            cash += order.priceTotal
            addParticle("+${formatMoneyCompact(order.priceTotal)} 🪙 Kas Masuk!", 0xFFF59E0B, 0.5f, 0.72f)
        }

        // Update quest progress
        activeQuests.find { it.id == "Q-1" }?.let { q ->
            q.currentProgress = min(q.targetAmount, q.currentProgress + order.weightKg)
        }
        activeQuests.find { it.id == "Q-3" }?.let { q ->
            q.currentProgress = reputationStars
        }
    }

    fun addParticle(text: String, colorHex: Long, xRatio: Float, yRatio: Float) {
        val now = System.currentTimeMillis()
        floatingParticles.removeAll { now - it.timestamp > 2500 }
        floatingParticles.add(FloatingParticle(UUID.randomUUID().toString(), text, colorHex, xRatio, yRatio))
    }

    fun onTaskCompleted(title: String, subtitle: String, xpEarned: Int, cashEarned: Double = 0.0, phase: StationPhase) {
        val now = System.currentTimeMillis()
        if (now - lastTaskCompletedTimestamp < 15_000) {
            taskComboCount++
        } else {
            taskComboCount = 1
        }
        lastTaskCompletedTimestamp = now

        val bonusComboXp = if (taskComboCount > 1) (taskComboCount * 2) else 0
        val totalXp = xpEarned + bonusComboXp
        addXp(totalXp)

        val feedback = TaskCompletionFeedback(
            title = title,
            subtitle = subtitle,
            xpEarned = totalXp,
            cashEarned = cashEarned,
            comboCount = taskComboCount,
            phase = phase,
            timestamp = now
        )
        latestCompletionFeedback = feedback

        val comboBadge = if (taskComboCount > 1) " 🔥x$taskComboCount" else ""
        addParticle("$title$comboBadge (+${totalXp}XP)", 0xFF0EA5E9, 0.5f, 0.35f)
    }

    fun interactWithStation(phase: StationPhase) {
        com.example.audio.GameSoundManager.playTapBoost()
        lastInteractedPhase = phase
        lastInteractionTimestamp = System.currentTimeMillis()

        val hasGasDryer = equipmentList.any { it.type == AssetType.DRYER_GAS_10KG }
        val hasBoiler = equipmentList.any { it.type == AssetType.SETRIKA_UAP_BOILER }

        when (phase) {
            StationPhase.MASUK -> {
                val ord = ordersList.firstOrNull { it.currentPhase == StationPhase.MASUK }
                if (ord != null) {
                    ord.minutesInCurrentPhase += 6
                    if (ord.minutesInCurrentPhase >= 12) {
                        ord.currentPhase = StationPhase.CUCI
                        ord.minutesInCurrentPhase = 0
                        addParticle("🧺 Masuk ➔ Cuci (${ord.weightKg}kg)!", 0xFF10B981, 0.25f, 0.48f)
                    }
                } else if (ordersList.count { !it.isCompleted } < 10) {
                    spawnCustomerOrder()
                }
            }
            StationPhase.CUCI -> {
                val ord = ordersList.firstOrNull { it.currentPhase == StationPhase.CUCI }
                val washer = equipmentList.firstOrNull {
                    (it.type == AssetType.MESIN_CUCI_7KG || it.type == AssetType.MESIN_CUCI_14KG)
                }
                if (ord != null) {
                    if (washer != null && !washer.isRunning) {
                        washer.isRunning = true
                        washer.remainingMinutesInCycle = washer.cycleMinutes
                        washer.currentBatchWeightKg = ord.weightKg
                        ord.requiredMinutesInPhase = washer.cycleMinutes
                        if (ord.minutesInCurrentPhase == 0) ord.minutesInCurrentPhase = 1
                    }
                    ord.minutesInCurrentPhase += 9
                    washer?.let {
                        it.remainingMinutesInCycle = max(0, it.remainingMinutesInCycle - 9)
                    }
                    val targetWashMins = ord.requiredMinutesInPhase.coerceIn(20, 45)
                    if (ord.minutesInCurrentPhase >= targetWashMins || (washer != null && washer.remainingMinutesInCycle <= 0)) {
                        washer?.isRunning = false
                        washer?.currentBatchWeightKg = 0.0
                        ord.currentPhase = StationPhase.JEMUR_KERING
                        ord.minutesInCurrentPhase = 0
                        addParticle("🫧 Selesai Cuci ➔ Kering!", 0xFF0EA5E9, 0.38f, 0.28f)
                    }
                }
            }
            StationPhase.JEMUR_KERING -> {
                val ord = ordersList.firstOrNull { it.currentPhase == StationPhase.JEMUR_KERING }
                if (ord != null) {
                    val baseDryMinutes = if (hasGasDryer) 45 else (180 * weatherToday.dryTimeMultiplier).roundToInt()
                    ord.minutesInCurrentPhase += if (hasGasDryer) 15 else 45
                    if (ord.minutesInCurrentPhase >= baseDryMinutes) {
                        ord.currentPhase = StationPhase.SETRIKA
                        ord.minutesInCurrentPhase = 0
                        addParticle("☀️ Kering ➔ Siap Setrika!", 0xFFF59E0B, 0.65f, 0.30f)
                    }
                }
            }
            StationPhase.SETRIKA -> {
                val ord = ordersList.firstOrNull { it.currentPhase == StationPhase.SETRIKA }
                if (ord != null) {
                    val requiredIronMinutes = (ord.weightKg * (if (hasBoiler) 5.0 else 9.0)).roundToInt().coerceAtLeast(15)
                    ord.minutesInCurrentPhase += 12
                    if (ord.minutesInCurrentPhase >= requiredIronMinutes) {
                        ord.currentPhase = StationPhase.PACKING
                        ord.minutesInCurrentPhase = 0
                        addParticle("💨 Licin Rapi ➔ Packing!", 0xFFF43F5E, 0.55f, 0.52f)
                    }
                }
            }
            StationPhase.PACKING -> {
                val ord = ordersList.firstOrNull { it.currentPhase == StationPhase.PACKING }
                if (ord != null) {
                    val requiredPackingMinutes = (ord.weightKg * 3.0).roundToInt().coerceAtLeast(10)
                    ord.minutesInCurrentPhase += 6
                    if (ord.minutesInCurrentPhase >= requiredPackingMinutes) {
                        completeAndPayOrder(ord)
                    }
                }
            }
            StationPhase.SELESAI -> {
                reputationStars = (reputationStars + 0.01).coerceAtMost(5.0)
                activeQuests.find { it.id == "Q-3" }?.let { it.currentProgress = reputationStars }
            }
        }
        onTaskCompleted(
            title = "⚡ Bantu ${phase.title}!",
            subtitle = "Pekerjaan dipercepat tangan pemilik",
            xpEarned = 8,
            phase = phase
        )
        addParticle("⚡ Cepat! +8 XP ⭐", 0xFFF59E0B, 0.5f, 0.45f)
    }

    fun addXp(amount: Int) {
        if (amount <= 0) return
        currentXp += amount

        // Loop to support multi-level jumps if a large XP grant crosses multiple thresholds
        while (true) {
            val nextTier = GrowthTier.values().firstOrNull { it.levelNumber == tycoonLevel + 1 }
            if (nextTier == null || currentXp < nextTier.requiredXp) break

            val oldLevel = tycoonLevel
            val oldTier = growthTier
            val oldCap = growthTier.maxDailyCapacityTargetKg

            tycoonLevel = nextTier.levelNumber
            growthTier = nextTier
            val newCap = nextTier.maxDailyCapacityTargetKg
            maxDailyIntakeKgQuota = newCap

            val bonusCash = when (nextTier.levelNumber) {
                2 -> 750_000.0
                3 -> 1_500_000.0
                4 -> 3_000_000.0
                5 -> 6_000_000.0
                else -> 500_000.0
            }
            cash += bonusCash

            val perks = when (nextTier.levelNumber) {
                2 -> listOf(
                    "Throughput Maksimal melonjak ke ${newCap.toInt()} kg/hari",
                    "Bonus dana ekspansi +${formatMoney(bonusCash)}",
                    "Pilihan mesin komersial 14 kg terbuka",
                    "Kecepatan sortir & cuci tim naik +15%"
                )
                3 -> listOf(
                    "Throughput Maksimal melonjak ke ${newCap.toInt()} kg/hari",
                    "Bonus dana ekspansi +${formatMoney(bonusCash)}",
                    "Akses Dryer Gas Komersial (Anti-Hujan)",
                    "Pelanggan langganan korporat & kos bertambah"
                )
                4 -> listOf(
                    "Throughput Maksimal tembus ${newCap.toInt()} kg/hari",
                    "Bonus dana ekspansi +${formatMoney(bonusCash)}",
                    "Akses Setrika Uap Boiler Industri",
                    "Sistem garansi SLA reputasi bintang lima"
                )
                else -> listOf(
                    "Throughput Super Pabrik ${newCap.toInt()} kg/hari",
                    "Bonus dana ekspansi +${formatMoney(bonusCash)}",
                    "Kemitraan sentra laundry kota"
                )
            }

            pendingLevelUp = LevelUpCelebration(
                oldLevelNumber = oldLevel,
                newLevelNumber = nextTier.levelNumber,
                oldTierTitle = oldTier.title,
                newTierTitle = nextTier.title,
                oldCapacityKg = oldCap,
                newCapacityKg = newCap,
                bonusCash = bonusCash,
                unlockedPerks = perks
            )

            addParticle("🌟 LEVEL UP! KAPASITAS ${newCap.toInt()}kg!", 0xFFFBBF24, 0.5f, 0.25f)
        }
    }

    fun claimLevelUp() {
        pendingLevelUp = null
        addParticle("🚀 KAPASITAS BARU AKTIF!", 0xFF10B981, 0.5f, 0.3f)
    }

    fun dismissStreakCelebration() {
        pendingStreakCelebration = null
    }

    fun triggerTestLevelUp() {
        val nextTier = GrowthTier.values().firstOrNull { it.levelNumber == tycoonLevel + 1 }
        if (nextTier != null) {
            val neededXp = (nextTier.requiredXp - currentXp).coerceAtLeast(1)
            addXp(neededXp)
        } else {
            // If already at max level, cycle to tier 1 then upgrade to tier 2 (20kg -> 40kg) for demonstration
            tycoonLevel = 1
            growthTier = GrowthTier.RUMAHAN
            currentXp = 0
            maxDailyIntakeKgQuota = 20.0
            val next = GrowthTier.BERKEMBANG
            addXp(next.requiredXp)
        }
    }

    fun triggerTestStreak() {
        currentStreak++
        if (currentStreak > highestStreak) highestStreak = currentStreak
        val bonusCash = currentStreak * 75_000.0
        val bonusXp = currentStreak * 30
        cash += bonusCash
        addXp(bonusXp)
        pendingStreakCelebration = StreakCelebration(
            streakDays = currentStreak,
            bonusCash = bonusCash,
            bonusXp = bonusXp,
            title = "🔥 HARI EFISIEN BERTURUT-TURUT!",
            multiplierPercent = (currentStreak * 10).coerceAtMost(50)
        )
        addParticle("🔥 STREAK ${currentStreak}x! +${formatMoneyCompact(bonusCash)}", 0xFFF59E0B, 0.5f, 0.35f)
    }

    // TYCOON GAME BOOSTERS
    fun giveCoffeeBoost(): Boolean {
        val cost = 25_000.0
        if (cash < cost) return false
        cash -= cost
        operationalExpensesToday += cost
        for (w in workerList) {
            w.energyPercent = 100
            w.moodEmoji = "⚡"
        }
        addParticle("☕ KOPI BOOSTER! Energi 100%", 0xFF38BDF8, 0.5f, 0.5f)
        activeQuests.find { it.id == "Q-2" }?.let { it.currentProgress = 1.0 }
        return true
    }

    fun activateTurboWash(): Boolean {
        for (eq in equipmentList) {
            if (eq.isRunning) {
                eq.remainingMinutesInCycle = max(1, eq.remainingMinutesInCycle / 2)
            }
        }
        for (ord in ordersList.filter { it.currentPhase == StationPhase.CUCI && it.minutesInCurrentPhase > 0 }) {
            ord.minutesInCurrentPhase = max(ord.minutesInCurrentPhase, ord.requiredMinutesInPhase / 2)
        }
        addParticle("⚡ TURBO WASH AKTIF!", 0xFF38BDF8, 0.22f, 0.30f)
        return true
    }

    fun triggerPromoFlyer(): Boolean {
        val cost = 40_000.0
        if (cash < cost) return false
        cash -= cost
        operationalExpensesToday += cost
        spawnCustomerOrder()
        spawnCustomerOrder()
        addParticle("📢 BROSUR SEBAR! +2 Order", 0xFF10B981, 0.22f, 0.70f)
        return true
    }

    fun claimQuest(questId: String): Boolean {
        val q = activeQuests.find { it.id == questId && !it.isClaimed && it.currentProgress >= it.targetAmount }
        if (q != null) {
            q.isClaimed = true
            cash += q.rewardMoney
            addXp(q.rewardXp)
            addParticle("+${formatMoneyCompact(q.rewardMoney)} 💰 +${q.rewardXp} XP ⭐", 0xFFFBBF24, 0.5f, 0.4f)
            return true
        }
        return false
    }

    // Zero Late Orders & Visual Satisfaction
    fun getZeroLateStreakInfo(): ZeroLateStreakInfo {
        val totalCompleted = completedOrdersCountToday
        val onTimePercent = if (totalCompleted > 0) {
            val onTimeCount = (totalCompleted - lateOrdersCountToday).coerceAtLeast(0)
            (onTimeCount.toDouble() / totalCompleted * 100.0).coerceIn(0.0, 100.0)
        } else {
            100.0
        }
        val mult = 1.0 + (zeroLateStreakDays * 0.08).coerceAtMost(0.60)
        val bonusCash = zeroLateStreakDays * 50_000.0
        val tier = when {
            zeroLateStreakDays >= 5 -> "👑 LEGENDARY FLAWLESS ON-TIME"
            zeroLateStreakDays >= 3 -> "⚡ SUPER DISIPLIN TEPAT WAKTU"
            zeroLateStreakDays >= 2 -> "🌟 KONSISTEN BEBAS TELAT"
            zeroLateStreakDays >= 1 -> "🛡️ DISIPLIN STANDAR"
            else -> "🎯 TARGET: 0 PESANAN TELAT"
        }
        return ZeroLateStreakInfo(
            currentStreakDays = zeroLateStreakDays,
            highestStreakDays = highestZeroLateStreakDays,
            todayLateOrders = lateOrdersCountToday,
            todayCompletedOrders = completedOrdersCountToday,
            todayOnTimePercentage = onTimePercent,
            visualSatisfactionBonusMultiplier = mult,
            bonusCashAccumulated = bonusCash,
            tierTitle = tier
        )
    }

    fun claimVisualSatisfactionBonus(): Boolean {
        if (satisfactionBonusClaimedToday) return false
        satisfactionBonusClaimedToday = true
        val bonusCashReward = (zeroLateStreakDays.coerceAtLeast(1) * 40_000.0)
        cash += bonusCashReward
        addXp(30 * zeroLateStreakDays.coerceAtLeast(1))
        reputationStars = (reputationStars + 0.05).coerceAtMost(5.0)
        activeQuests.find { it.id == "Q-3" }?.let { it.currentProgress = reputationStars }

        addParticle("✨ PUAS MAKSIMAL! +${formatMoneyCompact(bonusCashReward)} 🪙", 0xFF10B981, 0.5f, 0.35f)
        addParticle("⭐ Rating Konsumen ⭐ 5.0", 0xFFF59E0B, 0.52f, 0.45f)
        addParticle("💖 Pelanggan Puas 100%!", 0xFFEC4899, 0.48f, 0.25f)
        return true
    }

    // Bottleneck Analysis
    fun diagnoseBottleneck(): BottleneckDiagnosis {
        val masukKg = ordersList.filter { it.currentPhase == StationPhase.MASUK }.sumOf { it.weightKg }
        val cuciKg = ordersList.filter { it.currentPhase == StationPhase.CUCI }.sumOf { it.weightKg }
        val keringKg = ordersList.filter { it.currentPhase == StationPhase.JEMUR_KERING }.sumOf { it.weightKg }
        val setrikaKg = ordersList.filter { it.currentPhase == StationPhase.SETRIKA }.sumOf { it.weightKg }

        val hasGasDryer = equipmentList.any { it.type == AssetType.DRYER_GAS_10KG }
        val setrikaStaff = workerList.count { it.assignedPhase == StationPhase.SETRIKA }

        return when {
            weatherToday == Weather.HUJAN && !hasGasDryer && keringKg > 10.0 -> {
                BottleneckDiagnosis(
                    phase = StationPhase.JEMUR_KERING,
                    loadKg = keringKg,
                    waitMinutes = 360,
                    cause = "Hujan turun! Jemuran biasa jadi lambat. Ketuk stasiun Kering untuk bantu angkat jemuran!",
                    suggestion = "Beli Dryer Gas di Toko agar cucian cepat kering meski hujan."
                )
            }
            setrikaKg >= 14.0 && setrikaStaff <= 1 -> {
                BottleneckDiagnosis(
                    phase = StationPhase.SETRIKA,
                    loadKg = setrikaKg,
                    waitMinutes = 90,
                    cause = "Tumpukan baju di Setrika mulai tinggi! Ketuk meja Setrika untuk bantu menyetrika!",
                    suggestion = "Pindahkan 1 staf ke Setrika atau beli Setrika Uap Boiler."
                )
            }
            cuciKg >= 20.0 -> {
                BottleneckDiagnosis(
                    phase = StationPhase.CUCI,
                    loadKg = cuciKg,
                    waitMinutes = 75,
                    cause = "Mesin cuci sedang penuh antrean! Ketuk Mesin Cuci atau pakai tombol ⚡ Turbo!",
                    suggestion = "Tambah Mesin Cuci baru di Toko Alat agar cucian lebih cepat."
                )
            }
            masukKg >= 20.0 -> {
                BottleneckDiagnosis(
                    phase = StationPhase.MASUK,
                    loadKg = masukKg,
                    waitMinutes = 45,
                    cause = "Banyak pelanggan menaruh baju kotor di Meja Kasir! Ketuk stasiun Masuk untuk timbang cepat!",
                    suggestion = "Bantu sortir di stasiun Masuk agar langsung masuk ke Mesin Cuci."
                )
            }
            else -> {
                // Pick whichever active station currently has the highest load so the guide always points to real work!
                val busiest = StationPhase.values()
                    .filter { it != StationPhase.SELESAI }
                    .maxByOrNull { ph -> ordersList.filter { it.currentPhase == ph }.sumOf { it.weightKg } }
                    ?: StationPhase.CUCI
                val busiestKg = ordersList.filter { it.currentPhase == busiest }.sumOf { it.weightKg }
                if (busiestKg > 0.0) {
                    BottleneckDiagnosis(
                        phase = busiest,
                        loadKg = busiestKg,
                        waitMinutes = 15,
                        cause = "Ada ${busiestKg.toInt()} kg cucian di stasiun ${busiest.title}. Ketuk di Ruang 3D untuk mempercepat!",
                        suggestion = "Ketuk langsung alat 3D ${busiest.icon} ${busiest.title} untuk dapat bonus XP!"
                    )
                } else {
                    BottleneckDiagnosis(
                        phase = StationPhase.SETRIKA,
                        loadKg = setrikaKg,
                        waitMinutes = 20,
                        cause = "Semua stasiun lancar! Ketuk Meja Masuk (①) untuk panggil pelanggan baru!",
                        suggestion = "Pertahankan kerja cepat agar pelanggan memberi bintang 5!"
                    )
                }
            }
        }
    }

    // Close Current Day & Evaluate
    fun closeDayAndGenerateReport(): DailySummaryReport {
        val dailyWagesTotal = workerList.sumOf { it.dailySalary + (if (it.isOvertime) 40_000.0 else 0.0) }
        operationalExpensesToday += dailyWagesTotal
        cash -= dailyWagesTotal

        val totalOrdersToday = ordersList.filter { it.arrivalDay == currentDay }.size
        val totalKgToday = ordersList.filter { it.arrivalDay == currentDay }.sumOf { it.weightKg }
        val netProfit = revenueCollectedToday - operationalExpensesToday
        val bottleneck = diagnoseBottleneck()
        val leftoverBacklog = ordersList.filter { !it.isCompleted }.sumOf { it.weightKg }

        val completedInList = ordersList.filter { it.isCompleted }
        val onTimePercent = if (completedInList.isNotEmpty()) {
            val onTimeCount = completedInList.count { !it.isLate }
            (onTimeCount.toDouble() / completedInList.size) * 100.0
        } else {
            100.0
        }

        val keyInsight = when {
            netProfit > 200_000 -> "Hari yang sangat produktif! Utilisasi optimal menghasilkan laba bersih positif."
            leftoverBacklog > 15 -> "Terjadi backlog ${leftoverBacklog.roundToInt()} kg di tahap ${bottleneck.phase.title}. Perlu penyesuaian alokasi besok."
            weatherToday == Weather.HUJAN -> "Cuaca buruk memperlambat pengeringan. Pertimbangkan investasi dryer untuk mencegah keterlambatan."
            else -> "Operasi berjalan lancar. Pantau kelelahan pekerja untuk mencegah rework."
        }

        // Efficiency Streak Evaluation
        val isEfficientDay = onTimePercent >= 75.0 && netProfit > 0 && completedKgToday >= 6.0
        var streakBonusCash = 0.0
        var isMaintained = false

        if (isEfficientDay) {
            currentStreak++
            isMaintained = true
            if (currentStreak > highestStreak) highestStreak = currentStreak
            streakBonusCash = currentStreak * 75_000.0
            val streakBonusXp = currentStreak * 30
            cash += streakBonusCash
            addXp(streakBonusXp)

            if (currentStreak >= 2) {
                pendingStreakCelebration = StreakCelebration(
                    streakDays = currentStreak,
                    bonusCash = streakBonusCash,
                    bonusXp = streakBonusXp,
                    title = "🔥 RENTETAN HARI EFISIEN!",
                    multiplierPercent = (currentStreak * 10).coerceAtMost(50)
                )
            }
            addParticle("🔥 STREAK ${currentStreak}x! +${formatMoneyCompact(streakBonusCash)} Bonus", 0xFFF59E0B, 0.5f, 0.35f)
        } else {
            currentStreak = 0
            isMaintained = false
        }

        // Zero Late Orders Streak Evaluation
        val hasZeroLateToday = lateOrdersCountToday == 0
        if (hasZeroLateToday) {
            zeroLateStreakDays++
            if (zeroLateStreakDays > highestZeroLateStreakDays) highestZeroLateStreakDays = zeroLateStreakDays
            val zeroLateBonus = zeroLateStreakDays * 40_000.0
            cash += zeroLateBonus
            addXp(zeroLateStreakDays * 20)
            addParticle("🛡️ STREAK BEBAS TELAT ${zeroLateStreakDays} HARI! +${formatMoneyCompact(zeroLateBonus)}", 0xFF10B981, 0.5f, 0.35f)
        } else {
            zeroLateStreakDays = 0
            addParticle("💔 Streak Bebas Telat Terputus ($lateOrdersCountToday telat)", 0xFFEF4444, 0.5f, 0.35f)
        }

        val report = DailySummaryReport(
            day = currentDay,
            totalOrdersReceived = totalOrdersToday,
            totalKgReceived = totalKgToday,
            totalOrdersCompleted = completedOrdersCountToday,
            totalKgCompleted = completedKgToday,
            onTimePercentage = onTimePercent,
            revenueToday = revenueCollectedToday,
            operatingExpensesToday = operationalExpensesToday,
            netProfitToday = netProfit,
            endingCash = cash,
            bottleneckPhase = bottleneck.phase,
            keyInsight = keyInsight,
            leftoverBacklogKg = leftoverBacklog,
            efficiencyStreak = currentStreak,
            streakBonusCash = streakBonusCash,
            isStreakMaintained = isMaintained,
            zeroLateStreak = zeroLateStreakDays
        )
        historyReports.add(report)

        return report
    }

    fun startNextDay() {
        currentDay++
        currentMinuteOfDay = startWorkMinute
        simSpeed = SimSpeed.SPEED_1X
        revenueCollectedToday = 0.0
        operationalExpensesToday = 0.0
        completedOrdersCountToday = 0
        completedKgToday = 0.0
        reworkCountToday = 0
        lateOrdersCountToday = 0
        satisfactionBonusClaimedToday = false

        for (w in workerList) {
            w.energyPercent = 100
            w.isOvertime = false
            w.moodEmoji = "😊"
        }

        weatherToday = when (currentDay % 4) {
            1 -> Weather.CERAH
            2 -> Weather.MENDUNG
            3 -> Weather.HUJAN
            else -> Weather.CERAH
        }

        refreshDailyQuests()
    }

    fun buyEquipment(assetType: AssetType): Boolean {
        if (cash < assetType.purchaseCost) return false
        cash -= assetType.purchaseCost
        val newId = "EQ-${equipmentList.size + 1}"
        equipmentList.add(EquipmentItem(newId, assetType))
        addParticle("🎉 Beli ${assetType.displayName}!", 0xFF38BDF8, 0.5f, 0.5f)
        addXp(50)
        return true
    }

    fun hireWorker(name: String, phase: StationPhase): Boolean {
        val hiringCost = 500_000.0
        if (cash < hiringCost) return false
        cash -= hiringCost
        val newId = "W-${workerList.size + 1}"
        workerList.add(WorkerStaff(newId, name, "Operator Baru", 0xFF8B5CF6, assignedPhase = phase))
        addParticle("👋 Rekrut $name!", 0xFF10B981, 0.5f, 0.5f)
        addXp(40)
        return true
    }

    fun cloneForLab(): SimulationEngine {
        val lab = SimulationEngine(isLabMode = true)
        lab.currentDay = this.currentDay
        lab.currentMinuteOfDay = this.currentMinuteOfDay
        lab.weatherToday = this.weatherToday
        lab.cash = this.cash
        lab.reputationStars = this.reputationStars
        lab.repeatCustomerRatePercent = this.repeatCustomerRatePercent
        lab.growthTier = this.growthTier
        lab.currentXp = this.currentXp
        lab.tycoonLevel = this.tycoonLevel
        lab.currentStreak = this.currentStreak
        lab.highestStreak = this.highestStreak
        lab.zeroLateStreakDays = this.zeroLateStreakDays
        lab.highestZeroLateStreakDays = this.highestZeroLateStreakDays
        lab.lateOrdersCountToday = this.lateOrdersCountToday
        lab.satisfactionBonusClaimedToday = this.satisfactionBonusClaimedToday
        lab.taskComboCount = this.taskComboCount
        lab.maxDailyIntakeKgQuota = this.maxDailyIntakeKgQuota
        lab.isAcceptingNewOrdersToday = this.isAcceptingNewOrdersToday
        lab.isOvertimeActiveToday = this.isOvertimeActiveToday
        lab.revenueCollectedToday = this.revenueCollectedToday
        lab.operationalExpensesToday = this.operationalExpensesToday
        lab.completedOrdersCountToday = this.completedOrdersCountToday
        lab.completedKgToday = this.completedKgToday
        lab.reworkCountToday = this.reworkCountToday

        lab.equipmentList.clear()
        this.equipmentList.forEach { lab.equipmentList.add(it.copy()) }

        lab.workerList.clear()
        this.workerList.forEach { lab.workerList.add(it.copy()) }

        lab.ordersList.clear()
        this.ordersList.forEach { lab.ordersList.add(it.copy()) }

        lab.activeQuests.clear()
        this.activeQuests.forEach { lab.activeQuests.add(it.copy()) }

        lab.simSpeed = SimSpeed.SPEED_1X
        return lab
    }
}
