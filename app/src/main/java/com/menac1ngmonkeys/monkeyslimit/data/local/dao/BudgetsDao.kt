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

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budgets>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetById(id: Int): Flow<Budgets?>

    @Update
    suspend fun update(budgets: Budgets)

    @Delete
    suspend fun delete(budgets: Budgets)
}
