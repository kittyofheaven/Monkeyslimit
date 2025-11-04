package com.menac1ngmonkeys.monkeyslimit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.*
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.*

@Database(
    entities = [
        Budget::class, 
        Category::class, 
        Item::class, 
        Member::class, 
        MemberItems::class, 
        Notification::class, 
        SmartSplit::class, 
        Transaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun itemDao(): ItemDao
    abstract fun memberDao(): MemberDao
    abstract fun memberItemsDao(): MemberItemsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun smartSplitDao(): SmartSplitDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monkeyslimit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
