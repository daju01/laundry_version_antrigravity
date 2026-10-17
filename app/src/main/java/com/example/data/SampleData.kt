package com.example.data

import com.example.model.BusinessConfig
import com.example.model.LaundryOrder
import com.example.model.OrderStatus
import com.example.model.ServiceType

object SampleData {
    val defaultBusinessConfig = BusinessConfig(
        businessName = "Laundry Bersih",
        regularPricePerKg = 9_000.0,
        expressPricePerKg = 15_000.0,
        targetKgPerDay = 120.0,
        operatingHoursPerDay = 12.0,
        operatingEfficiency = 0.80,
        washingMachineCount = 3,
        washingCapacityPerMachineKg = 10.0,
        washingCycleDurationMinutes = 45.0,
        dryerCount = 2,
        dryerCapacityPerMachineKg = 10.0,
        dryerCycleDurationMinutes = 45.0,
        ironCount = 2,
        ironCapacityPerUnitPerHourKg = 10.0,
        staffCount = 4,
        salaryPerStaffMonthly = 3_000_000.0,
        rentMonthly = 6_000_000.0,
        internetSoftwareMonthly = 500_000.0,
        depreciationMonthly = 1_000_000.0,
        adminOtherMonthly = 500_000.0,
        electricityMonthlyBase = 3_500_000.0,
        waterMonthlyBase = 1_500_000.0,
        chemicalPerKg = 1_200.0,
        plasticPerKg = 600.0,
        gasAndOtherPerKg = 800.0,
        initialCash = 25_000_000.0
    )

    fun getInitialOrders(): List<LaundryOrder> {
        val now = System.currentTimeMillis()
        val oneHour = 3600_000L

        return listOf(
            // TAHAP 1: MASUK (Total 42 kg)
            LaundryOrder("ORD-001", "Pak Budi Hartono", "08123456781", ServiceType.REGULER, 12.0, 9_000.0, deadline = "Besok", status = OrderStatus.MASUK, createdAt = now - oneHour * 2),
            LaundryOrder("ORD-002", "Ibu Rina Wijaya", "08139876542", ServiceType.CUCI_SETRIKA, 8.0, 9_000.0, deadline = "2 hari", status = OrderStatus.MASUK, createdAt = now - oneHour * 3),
            LaundryOrder("ORD-003", "Kak Maya Sari", "08215544332", ServiceType.EXPRESS, 10.0, 15_000.0, deadline = "Hari ini", status = OrderStatus.MASUK, createdAt = now - oneHour * 1),
            LaundryOrder("ORD-004", "Mas Dimas Arya", "08567788990", ServiceType.REGULER, 7.0, 9_000.0, deadline = "Besok", status = OrderStatus.MASUK, createdAt = now - oneHour * 4),
            LaundryOrder("ORD-005", "Bu Ani Kartika", "08781234567", ServiceType.REGULER, 5.0, 9_000.0, deadline = "Besok", status = OrderStatus.MASUK, createdAt = now - oneHour * 2),

            // TAHAP 2: CUCI (Total 28 kg)
            LaundryOrder("ORD-006", "Bu Sri Mulyani", "08129088771", ServiceType.EXPRESS, 10.0, 15_000.0, deadline = "Hari ini", status = OrderStatus.CUCI, createdAt = now - oneHour * 4),
            LaundryOrder("ORD-007", "Pak Doni Pratama", "08131122334", ServiceType.REGULER, 8.0, 9_000.0, deadline = "Besok", status = OrderStatus.CUCI, createdAt = now - oneHour * 5),
            LaundryOrder("ORD-008", "Ibu Larasati", "08154433221", ServiceType.CUCI_SETRIKA, 10.0, 9_000.0, deadline = "Besok", status = OrderStatus.CUCI, createdAt = now - oneHour * 6),

            // TAHAP 3: KERING (Total 17 kg)
            LaundryOrder("ORD-009", "Mas Reza Pahlevi", "08223344556", ServiceType.REGULER, 9.0, 9_000.0, deadline = "Besok", status = OrderStatus.KERING, createdAt = now - oneHour * 6),
            LaundryOrder("ORD-010", "Bu Eka Septia", "08778899001", ServiceType.CUCI_SAJA, 8.0, 6_000.0, deadline = "Hari ini", status = OrderStatus.KERING, createdAt = now - oneHour * 7),

            // TAHAP 4: SETRIKA (Total 36 kg)
            LaundryOrder("ORD-011", "Ibu Dewi Lestari", "08128877665", ServiceType.REGULER, 12.0, 9_000.0, deadline = "Hari ini", status = OrderStatus.SETRIKA, createdAt = now - oneHour * 8),
            LaundryOrder("ORD-012", "Pak Hadi Nugroho", "08136655443", ServiceType.SETRIKA_SAJA, 10.0, 6_000.0, deadline = "Hari ini", status = OrderStatus.SETRIKA, createdAt = now - oneHour * 9),
            LaundryOrder("ORD-013", "Mas Danang Prasetyo", "08571234890", ServiceType.EXPRESS, 8.0, 15_000.0, deadline = "Hari ini", status = OrderStatus.SETRIKA, createdAt = now - oneHour * 5),
            LaundryOrder("ORD-014", "Bu Sinta Rosalina", "08189900112", ServiceType.REGULER, 6.0, 9_000.0, deadline = "Hari ini", status = OrderStatus.SETRIKA, createdAt = now - oneHour * 10),

            // TAHAP 5: PACKING (Total 12 kg)
            LaundryOrder("ORD-015", "Mbak Fitri Handayani", "08219988776", ServiceType.REGULER, 7.0, 9_000.0, deadline = "Hari ini", status = OrderStatus.PACKING, createdAt = now - oneHour * 11),
            LaundryOrder("ORD-016", "Mas Bayu Wicaksono", "08561122334", ServiceType.REGULER, 5.0, 9_000.0, deadline = "Hari ini", status = OrderStatus.PACKING, createdAt = now - oneHour * 12),

            // TAHAP 6: SELESAI (Total 63 kg)
            LaundryOrder("ORD-017", "Ibu Ratna Juwita", "08123344551", ServiceType.EXPRESS, 11.0, 15_000.0, deadline = "Hari ini", status = OrderStatus.SELESAI, createdAt = now - oneHour * 14),
            LaundryOrder("ORD-018", "Pak Gunawan", "08134455662", ServiceType.REGULER, 9.0, 9_000.0, deadline = "Kemarin", status = OrderStatus.SELESAI, createdAt = now - oneHour * 16),
            LaundryOrder("ORD-019", "Kak Cindy Claudia", "08225566773", ServiceType.REGULER, 14.0, 9_000.0, deadline = "Kemarin", status = OrderStatus.SELESAI, createdAt = now - oneHour * 18),
            LaundryOrder("ORD-020", "Mas Anton Sanjaya", "08576677884", ServiceType.REGULER, 8.0, 9_000.0, deadline = "Hari ini", status = OrderStatus.SELESAI, createdAt = now - oneHour * 15),
            LaundryOrder("ORD-021", "Ibu Megawati", "08787788995", ServiceType.REGULER, 12.0, 9_000.0, deadline = "Kemarin", status = OrderStatus.SELESAI, createdAt = now - oneHour * 20),
            LaundryOrder("ORD-022", "Pak Hendra", "08129900116", ServiceType.REGULER, 9.0, 9_000.0, deadline = "Hari ini", status = OrderStatus.SELESAI, createdAt = now - oneHour * 13),

            // TAHAP 7: DIAMBIL (Contoh histori yang sudah selesai lunas diambil)
            LaundryOrder("ORD-023", "Bu Titi Kamal", "08131234900", ServiceType.REGULER, 14.0, 9_000.0, deadline = "Kemarin", status = OrderStatus.DIAMBIL, createdAt = now - oneHour * 24),
            LaundryOrder("ORD-024", "Mas Fajar", "08569876123", ServiceType.EXPRESS, 10.0, 15_000.0, deadline = "Kemarin", status = OrderStatus.DIAMBIL, createdAt = now - oneHour * 25)
        )
    }
}
