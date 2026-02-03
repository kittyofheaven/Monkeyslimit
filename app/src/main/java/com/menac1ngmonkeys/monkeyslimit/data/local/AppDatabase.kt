package com.menac1ngmonkeys.monkeyslimit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.*
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.*
import java.util.Date

@Database(
    entities = [
        Budgets::class,
        Categories::class,
        Items::class,
        Members::class,
        MemberItems::class,
        Notifications::class,
        SmartSplits::class,
        Transactions::class,
        User::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(DateConverter::class, Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetsDao(): BudgetsDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun itemsDao(): ItemsDao
    abstract fun membersDao(): MembersDao
    abstract fun memberItemsDao(): MemberItemsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun smartSplitsDao(): SmartSplitsDao
    abstract fun transactionsDao(): TransactionsDao
    abstract fun userDao(): UserDao

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

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}