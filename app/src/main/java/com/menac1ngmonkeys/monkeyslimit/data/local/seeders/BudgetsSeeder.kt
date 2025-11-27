package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets

object BudgetsSeeder {
    suspend fun seed(budgetsDao: BudgetsDao): Map<String, Int> {
        return if (budgetsDao.count() == 0) {
            val start = SeedUtils.firstDayOfCurrentMonth()
            val budgets = listOf(
                // Household Monthly
                Budgets(
                    id = 0,
                    name = "Household Monthly",
                    amount = 13_000_000.0,
                    limitAmount = 30_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Kebutuhan rumah tangga bulanan"
                ),

                // Groceries
                Budgets(
                    id = 0,
                    name = "Groceries",
                    amount = 4_400_000.0,
                    limitAmount = 8_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Belanja dapur dan kebutuhan makanan"
                ),

                // Transport
                Budgets(
                    id = 0,
                    name = "Transport",
                    amount = 1_600_000.0,
                    limitAmount = 4_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Transportasi harian"
                ),

                // Shopping
                Budgets(
                    id = 0,
                    name = "Shopping",
                    amount = 5_500_000.0,
                    limitAmount = 12_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Belanja fashion, elektronik, dan kebutuhan pribadi"
                ),

                // Bills
                Budgets(
                    id = 0,
                    name = "Bills",
                    amount = 2_800_000.0,
                    limitAmount = 5_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Tagihan bulanan: listrik, air, internet, streaming"
                ),

                // Entertainment
                Budgets(
                    id = 0,
                    name = "Entertainment",
                    amount = 1_200_000.0,
                    limitAmount = 3_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Hiburan: film, game, karaoke, rekreasi"
                ),

                // Health
                Budgets(
                    id = 0,
                    name = "Health",
                    amount = 2_200_000.0,
                    limitAmount = 6_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Kesehatan: obat, klinik, vitamin, asuransi"
                ),

                // Education
                Budgets(
                    id = 0,
                    name = "Education",
                    amount = 1_800_000.0,
                    limitAmount = 5_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Pendidikan: kursus, buku, alat tulis, sekolah"
                ),

                // Other
                Budgets(
                    id = 0,
                    name = "Other",
                    amount = 1_000_000.0,
                    limitAmount = 3_000_000.0,
                    startDate = start,
                    endDate = null,
                    note = "Kebutuhan lain-lain di luar kategori utama"
                )
            )
            budgets.associate { it.name to budgetsDao.insert(it).toInt() }
        } else {
            budgetsDao.getAllNow().associate { it.name to it.id }
        }
    }
}

