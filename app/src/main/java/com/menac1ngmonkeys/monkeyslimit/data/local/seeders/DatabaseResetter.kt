package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import android.content.Context
import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseResetter {
    /**
     * Soft reset: clear all tables on current DB instance and reseed initial data.
     */
    suspend fun resetAndReseed(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        db.clearAllTables()
        CategoriesSeeder.seed(db.categoriesDao())
    }
}
