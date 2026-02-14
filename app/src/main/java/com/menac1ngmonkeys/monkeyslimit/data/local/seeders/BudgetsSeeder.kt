package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets

object BudgetsSeeder {
    suspend fun seed(budgetsDao: BudgetsDao): Map<String, Int> {
        return if (budgetsDao.count() == 0) {
            val start = SeedUtils.firstDayOfCurrentMonth()

            val budgets = listOf(
                // 1. Needs
                Budgets(
                    id = 0,
                    name = "Needs (Food and Drinks)",
                    amount = 0.0,
                    limitAmount = 0.0,
                    startDate = start,
                    endDate = null,
                    note = "Essential daily expenses for sustenance."
                ),

                // 2. Education & Health
                Budgets(
                    id = 0,
                    name = "Education and Health",
                    amount = 0.0,
                    limitAmount = 0.0,
                    startDate = start,
                    endDate = null,
                    note = "Medical bills, courses, books, and wellness."
                ),

                // 3. Transportation
                Budgets(
                    id = 0,
                    name = "Transportation",
                    amount = 0.0,
                    limitAmount = 0.0,
                    startDate = start,
                    endDate = null,
                    note = "Commute costs, fuel, public transport, or vehicle maintenance."
                ),

                // 4. Wants
                Budgets(
                    id = 0,
                    name = "Wants (Lifestyle)",
                    amount = 0.0,
                    limitAmount = 0.0,
                    startDate = start,
                    endDate = null,
                    note = "Non-essential spending like shopping, entertainment, and dining out."
                ),

                // 5. Bills & Housing
                Budgets(
                    id = 0,
                    name = "Bills and Housing",
                    amount = 0.0,
                    limitAmount = 0.0,
                    startDate = start,
                    endDate = null,
                    note = "Rent/mortgage, utilities, internet, and phone bills."
                ),

                // 6. Investment & Donation
                Budgets(
                    id = 0,
                    name = "Investment and Donation",
                    amount = 0.0,
                    limitAmount = 0.0,
                    startDate = start,
                    endDate = null,
                    note = "Future savings, stocks, mutual funds, and charitable giving."
                )
            )

            // Insert all and return map of Name -> ID
            budgets.associate { it.name to budgetsDao.insert(it).toInt() }
        } else {
            // If already seeded, just return existing map
            budgetsDao.getAllNow().associate { it.name to it.id }
        }
    }
}