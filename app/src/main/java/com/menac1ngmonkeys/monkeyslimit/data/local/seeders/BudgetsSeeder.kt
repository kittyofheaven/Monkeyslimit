package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets

object BudgetsSeeder {
    suspend fun seed(budgetsDao: BudgetsDao): Map<String, Int> {
        return if (budgetsDao.count() == 0) {
            val start = SeedUtils.firstDayOfCurrentMonth()
            val budgets = listOf(
                Budgets(id = 0, name = "Household Monthly", amount = 13_000_000.0, limitAmount = 30_000_000.0, startDate = start, endDate = null, note = "Kebutuhan rumah tangga bulanan"),
                Budgets(id = 0, name = "Groceries", amount = 4_400_000.0, limitAmount = 8_000_000.0, startDate = start, endDate = null, note = "Belanja dapur dan kebutuhan makanan"),
                Budgets(id = 0, name = "Transport", amount = 1_600_000.0, limitAmount = 4_000_000.0, startDate = start, endDate = null, note = "Transportasi harian")
            )
            budgets.associate { it.name to budgetsDao.insert(it).toInt() }
        } else {
            budgetsDao.getAllNow().associate { it.name to it.id }
        }
    }
}

