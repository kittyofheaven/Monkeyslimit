package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.TransactionsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions

object TransactionsSeeder {
    suspend fun seed(
        transactionsDao: TransactionsDao,
        budgetsByName: Map<String, Int>,
        categoriesByName: Map<String, Int>
    ) {
        if (transactionsDao.count() > 0) return

        val txList = listOf(
            Transactions(id = 0, date = SeedUtils.daysAgo(1), totalAmount = 910_000.0, note = "Lunch with team", imagePath = null, budgetId = budgetsByName.getValue("Groceries"), categoryId = categoriesByName.getValue("Food and Beverages")),
            Transactions(id = 0, date = SeedUtils.daysAgo(2), totalAmount = 300_000.0, note = "Bensin motor", imagePath = null, budgetId = budgetsByName.getValue("Transport"), categoryId = categoriesByName.getValue("Transport")),
            Transactions(id = 0, date = SeedUtils.daysAgo(3), totalAmount = 2_500_000.0, note = "Belanja mingguan", imagePath = null, budgetId = budgetsByName.getValue("Groceries"), categoryId = categoriesByName.getValue("Food and Beverages")),
            Transactions(id = 0, date = SeedUtils.daysAgo(5), totalAmount = 1_200_000.0, note = "Listrik PLN", imagePath = null, budgetId = budgetsByName.getValue("Household Monthly"), categoryId = categoriesByName.getValue("Bills")),
            Transactions(id = 0, date = SeedUtils.daysAgo(7), totalAmount = 600_000.0, note = "Tiket film", imagePath = null, budgetId = budgetsByName.getValue("Household Monthly"), categoryId = categoriesByName.getValue("Entertainment")),
            Transactions(id = 0, date = SeedUtils.daysAgo(8), totalAmount = 450_000.0, note = "Obat flu", imagePath = null, budgetId = budgetsByName.getValue("Household Monthly"), categoryId = categoriesByName.getValue("Health"))
        )
        transactionsDao.insertAll(txList)
    }
}

