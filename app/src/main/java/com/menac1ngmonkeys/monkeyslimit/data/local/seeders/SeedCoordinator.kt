package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase

object SeedCoordinator {
    /**
     * Core seeds that every user must get (production-safe).
     * Keep minimal, deterministic, and idempotent.
     * Now includes Categories AND Budgets.
     */
    suspend fun seedCore(db: AppDatabase) {
        // 1. Seed Core Categories
        val categoriesDao = db.categoriesDao()
        if (categoriesDao.count() == 0) {
            CategoriesSeeder.seed(categoriesDao)
        }
    }

    /**
     * Development-only seeds with realistic data across tables.
     * DISABLED FOR PRODUCTION LAUNCH.
     */
    suspend fun seedDev(db: AppDatabase) {
        // Ensure Core data (Categories & Budgets) exists
        seedCore(db)

        // ====================================================================
        // DISABLED SEEDERS FOR PRODUCTION
        // Uncomment these if you ever need to test with dummy data again
        // ====================================================================

//        // 1. Get the current user's ID (Defaults to empty string if no one is logged in)
//        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
//
//        val categoriesByName = db.categoriesDao().getAllNow().associate { it.name to it.id }
//        val budgetsByName = db.budgetsDao().getAllNow("RtMVIsIO8sdg5T4Fsvdnf9rQHvt1").associate { it.name to it.id }
//
//        // Transactions
//        TransactionsSeeder.seed(db.transactionsDao(), budgetsByName, categoriesByName, "RtMVIsIO8sdg5T4Fsvdnf9rQHvt1")
//
//        // SmartSplits
//        val splitIds = SmartSplitsSeeder.seed(db.smartSplitsDao())
//
//        // Members + Items
//        val splitMembers = MembersSeeder.seed(db.membersDao(), splitIds)
//        val splitItems = ItemsSeeder.seed(db.itemsDao(), splitIds)
//
//        // MemberItems linkage
//        MemberItemsSeeder.seed(db.memberItemsDao(), splitMembers, splitItems)
//
//        // Notifications
//        NotificationsSeeder.seed(db.notificationsDao())

    }
}