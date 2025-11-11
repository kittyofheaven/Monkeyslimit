package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase

object SeedCoordinator {
    /**
     * Core seeds that every user must get (production-safe).
     * Keep minimal, deterministic, and idempotent. Currently: Categories only.
     */
    suspend fun seedCore(db: AppDatabase) {
        val categoriesDao = db.categoriesDao()
        if (categoriesDao.count() == 0) {
            CategoriesSeeder.seed(categoriesDao)
        }
    }

    /**
     * Development-only seeds with realistic data across tables.
     * Caller should guard with BuildConfig.DEBUG.
     */
    suspend fun seedDev(db: AppDatabase) {
        // Ensure Categories exist and build a mapping
        seedCore(db)
        val categoriesByName = db.categoriesDao().getAllNow().associate { it.name to it.id }

        // Budgets (mapping by name)
        val budgetsByName = BudgetsSeeder.seed(db.budgetsDao())

        // Transactions
        TransactionsSeeder.seed(db.transactionsDao(), budgetsByName, categoriesByName)

        // SmartSplits
        val splitIds = SmartSplitsSeeder.seed(db.smartSplitsDao())

        // Members + Items
        val splitMembers = MembersSeeder.seed(db.membersDao(), splitIds)
        val splitItems = ItemsSeeder.seed(db.itemsDao(), splitIds)

        // MemberItems linkage
        MemberItemsSeeder.seed(db.memberItemsDao(), splitMembers, splitItems)

        // Notifications
        NotificationsSeeder.seed(db.notificationsDao())
    }

    // Debug gating handled by callers (e.g., MainActivity)
}
