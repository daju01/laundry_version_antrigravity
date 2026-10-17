package com.example

import com.example.data.SampleData
import com.example.model.BusinessConfig
import com.example.model.BusinessEngine
import com.example.model.MachineInvestmentInput
import com.example.sim.*
import com.example.viewmodel.CockpitUiState
import org.junit.Assert.*
import org.junit.Test

class LaundryEngineUnitTest {

    @Test
    fun testSimulationEngineInitialState() {
        val engine = SimulationEngine()
        assertEquals(1, engine.currentDay)
        assertEquals(480, engine.currentMinuteOfDay) // 08:00
        assertEquals(5_000_000.0, engine.cash, 0.01) // Rp 5jt initial expansion cash
        assertEquals(2, engine.workerList.size) // Budi & Siti
        assertEquals(4, engine.equipmentList.size) // 1 washer, jemuran, setrika, packing
        assertTrue(engine.ordersList.isNotEmpty())
    }

    @Test
    fun testWeatherDryingImpact() {
        val sunny = Weather.CERAH
        val rainy = Weather.HUJAN
        assertTrue(rainy.dryTimeMultiplier > sunny.dryTimeMultiplier)
    }

    @Test
    fun testSimulationTickAndOrderProgression() {
        val engine = SimulationEngine()
        engine.simSpeed = SimSpeed.SPEED_1X
        val initialMins = engine.currentMinuteOfDay

        // Tick 60 minutes
        engine.tickMinutes(60)
        assertEquals(initialMins + 60, engine.currentMinuteOfDay)
    }

    @Test
    fun testEquipmentPurchase() {
        val engine = SimulationEngine()
        val initialCash = engine.cash
        val success = engine.buyEquipment(AssetType.SETRIKA_UAP_BOILER)
        assertTrue(success)
        assertEquals(initialCash - AssetType.SETRIKA_UAP_BOILER.purchaseCost, engine.cash, 0.01)
        assertTrue(engine.equipmentList.any { it.type == AssetType.SETRIKA_UAP_BOILER })
    }

    @Test
    fun testLabModeCloneIsolation() {
        val engine = SimulationEngine()
        val lab = engine.cloneForLab()

        assertTrue(lab.isLabMode)
        assertEquals(engine.cash, lab.cash, 0.01)

        // Modify lab with enough cash
        lab.cash = 20_000_000.0
        val success = lab.buyEquipment(AssetType.DRYER_GAS_10KG)
        assertTrue(success)
        // Original engine cash should remain intact
        assertNotEquals(engine.cash, lab.cash)
        assertFalse(engine.equipmentList.any { it.type == AssetType.DRYER_GAS_10KG })
        assertTrue(lab.equipmentList.any { it.type == AssetType.DRYER_GAS_10KG })
    }

    @Test
    fun testBEPCalculations() {
        val testConfig = BusinessConfig(
            regularPricePerKg = 9_000.0,
            chemicalPerKg = 1_000.0,
            plasticPerKg = 500.0,
            gasAndOtherPerKg = 500.0,
            electricityMonthlyBase = 1_500_000.0,
            waterMonthlyBase = 1_500_000.0,
            targetKgPerDay = 100.0,
            rentMonthly = 5_000_000.0,
            staffCount = 3,
            salaryPerStaffMonthly = 3_000_000.0,
            internetSoftwareMonthly = 500_000.0,
            depreciationMonthly = 500_000.0,
            adminOtherMonthly = 0.0
        )

        val bep = BusinessEngine.computeBEP(testConfig, actualDailyKg = 97.0)
        assertTrue(bep.bepKgPerMonth > 0)
        assertTrue(bep.bepKgPerDay > 0)
        assertEquals(97.0 - bep.bepKgPerDay, bep.gapKgPerDay, 0.01)
    }

    @Test
    fun testUnitEconomics() {
        val config = SampleData.defaultBusinessConfig
        val unitEco = BusinessEngine.computeUnitEconomics(config)

        assertTrue(unitEco.revenuePerKg == config.regularPricePerKg)
        assertTrue(unitEco.contributionMarginPerKg == unitEco.revenuePerKg - unitEco.variableCostPerKg)
        assertTrue(unitEco.netProfitPerKg == unitEco.contributionMarginPerKg - unitEco.fixedCostAllocationPerKg)
    }

    @Test
    fun testBottleneckDetection() {
        val state = CockpitUiState(
            config = SampleData.defaultBusinessConfig,
            orders = SampleData.getInitialOrders()
        )
        val pipeline = BusinessEngine.computePipeline(state)

        assertEquals("SETRIKA", pipeline.bottleneck.stage)
        assertTrue(pipeline.bottleneck.utilizationPercent > 0)
        assertNotNull(pipeline.bottleneck.recommendation)
    }

    @Test
    fun testTaskCompletionFeedbackAndCombo() {
        val engine = SimulationEngine()
        assertEquals(0, engine.taskComboCount)
        assertNull(engine.latestCompletionFeedback)

        // Simulate completing a washing task
        engine.onTaskCompleted("🫧 Cuci Selesai!", "Batch siap dikeringkan", 12, phase = StationPhase.CUCI)
        assertEquals(1, engine.taskComboCount)
        assertNotNull(engine.latestCompletionFeedback)
        assertEquals("🫧 Cuci Selesai!", engine.latestCompletionFeedback?.title)
        assertTrue(engine.currentXp > 30)

        // Rapid second task -> Combo x2
        engine.onTaskCompleted("💨 Licin Rapi!", "Siap di-packing", 15, phase = StationPhase.SETRIKA)
        assertEquals(2, engine.taskComboCount)
        assertEquals(2, engine.latestCompletionFeedback?.comboCount)
    }

    @Test
    fun testLevelUpCapacityExpansion() {
        val engine = SimulationEngine()
        assertEquals(1, engine.tycoonLevel)
        assertEquals(20.0, engine.growthTier.maxDailyCapacityTargetKg, 0.01)
        assertNull(engine.pendingLevelUp)

        // Add enough XP to level up to Level 2 (150 XP)
        engine.addXp(130) // 30 + 130 = 160 XP >= 150 XP
        assertEquals(2, engine.tycoonLevel)
        assertEquals(GrowthTier.BERKEMBANG, engine.growthTier)
        assertEquals(40.0, engine.growthTier.maxDailyCapacityTargetKg, 0.01)

        // Verify pending level-up celebration was generated
        val levelUp = engine.pendingLevelUp
        assertNotNull(levelUp)
        assertEquals(1, levelUp?.oldLevelNumber)
        assertEquals(2, levelUp?.newLevelNumber)
        assertEquals(20.0, levelUp?.oldCapacityKg ?: 0.0, 0.01)
        assertEquals(40.0, levelUp?.newCapacityKg ?: 0.0, 0.01)
        assertTrue(levelUp?.bonusCash ?: 0.0 > 0)
        assertTrue(levelUp?.unlockedPerks?.isNotEmpty() == true)

        // Claim level up
        engine.claimLevelUp()
        assertNull(engine.pendingLevelUp)
    }

    @Test
    fun testEfficiencyStreakOnDayClose() {
        val engine = SimulationEngine()
        engine.completedOrdersCountToday = 3
        engine.completedKgToday = 18.0
        engine.revenueCollectedToday = 450_000.0 // Healthy revenue > wages + op expenses
        engine.operationalExpensesToday = 50_000.0

        val initialCash = engine.cash
        val report = engine.closeDayAndGenerateReport()

        assertTrue(report.isStreakMaintained)
        assertTrue(engine.currentStreak >= 2)
        assertTrue(report.streakBonusCash > 0)
        assertTrue(engine.cash > initialCash - 150_000.0) // Gained bonus cash
    }
}
