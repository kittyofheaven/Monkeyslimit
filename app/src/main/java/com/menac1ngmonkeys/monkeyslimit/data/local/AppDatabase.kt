package com.menac1ngmonkeys.monkeyslimit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.*
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.*

@Database(
    entities = [
        Budgets::class,
        Categories::class,
        Items::class,
        Members::class,
        MemberItems::class,
        Notifications::class,
        SmartSplits::class,
        Transactions::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetsDao(): BudgetsDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun itemsDao(): ItemsDao
    abstract fun membersDao(): MembersDao
    abstract fun memberItemsDao(): MemberItemsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun smartSplitsDao(): SmartSplitsDao
    abstract fun transactionsDao(): TransactionsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monkeyslimit_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun hardReset(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                context.applicationContext.deleteDatabase("monkeyslimit_database")
                INSTANCE = null
            }
        }
    }
}
