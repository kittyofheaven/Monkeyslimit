package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: Transactions): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transactions>)

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transactions>>

    // Get specific type
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transactions>>

    // Calculate Total Income
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    // Calculate Total Expense
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): Flow<Transactions?>

    // Get all transactions for a specific budget
    @Query("SELECT * FROM transactions WHERE budgetId = :budgetId ORDER BY date DESC")
    fun getTransactionsByBudgetId(budgetId: Int): Flow<List<Transactions>>

    @Update
    suspend fun update(transactions: Transactions)

    @Delete
    suspend fun delete(transactions: Transactions)
}
