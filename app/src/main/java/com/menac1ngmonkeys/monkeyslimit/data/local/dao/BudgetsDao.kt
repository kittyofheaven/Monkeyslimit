package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budgets: Budgets): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<Budgets>)

    // Filter by userId
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: String): Flow<List<Budgets>>

    // Filter by userId
    @Query("SELECT COUNT(*) FROM budgets WHERE userId = :userId")
    suspend fun countUserBudgets(userId: String): Int

    // Filter by userId
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getAllNow(userId: String): List<Budgets>

    // Added userId to ensure users cannot fetch other users' budgets
    @Query("SELECT * FROM budgets WHERE id = :id AND userId = :userId")
    fun getBudgetById(id: Int, userId: String): Flow<Budgets?>

    // Global count (safe to leave as is for debug/system purposes)
    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun count(): Int

    @Update
    suspend fun update(budgets: Budgets)

    @Delete
    suspend fun delete(budgets: Budgets)
}