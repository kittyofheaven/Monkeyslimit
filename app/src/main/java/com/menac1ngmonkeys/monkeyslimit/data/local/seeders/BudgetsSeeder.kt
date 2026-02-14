package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets

object BudgetsSeeder {
    suspend fun seedForUser(budgetsDao: BudgetsDao, userId: String): Map<String, Int> {
        // 1. Only seed if this specific user has 0 budgets in the database
        return if (budgetsDao.countUserBudgets(userId) == 0) {
            val start = SeedUtils.firstDayOfCurrentMonth()

            val budgets = listOf(
                Budgets(0, "Needs (Food and Drinks)", 0.0, 0.0, start, null, "Essential daily expenses", userId = userId),
                Budgets(0, "Education and Health", 0.0, 0.0, start, null, "Medical bills, courses", userId = userId),
                Budgets(0, "Transportation", 0.0, 0.0, start, null, "Commute costs, fuel", userId = userId),
                Budgets(0, "Wants (Lifestyle)", 0.0, 0.0, start, null, "Non-essential spending", userId = userId),
                Budgets(0, "Bills and Housing", 0.0, 0.0, start, null, "Rent, utilities, internet", userId = userId),
                Budgets(0, "Investment and Donation", 0.0, 0.0, start, null, "Future savings", userId = userId)
            )

            // 2. Insert and return a mapping of Name to the newly generated ID
            budgets.associate { it.name to budgetsDao.insert(it).toInt() }
        } else {
            // 3. FIX: Only fetch budgets belonging to this specific userId to avoid data leaks
            budgetsDao.getAllNow(userId).associate { it.name to it.id }
        }
    }
}