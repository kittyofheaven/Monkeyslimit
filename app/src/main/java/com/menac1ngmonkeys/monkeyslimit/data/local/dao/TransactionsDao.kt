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

    // Filter by userId
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<Transactions>>

    // Get specific type, filtered by userId
    @Query("SELECT * FROM transactions WHERE type = :type AND userId = :userId ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType, userId: String): Flow<List<Transactions>>

    // Calculate Total Income for the specific user
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type = 'INCOME' AND userId = :userId")
    fun getTotalIncome(userId: String): Flow<Double?>

    // Calculate Total Expense for the specific user
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type = 'EXPENSE' AND userId = :userId")
    fun getTotalExpense(userId: String): Flow<Double?>

    // Global count (safe to leave as is)
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    // Added userId to prevent fetching other users' transactions
    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId")
    fun getTransactionById(id: Int, userId: String): Flow<Transactions?>

    // Get all transactions for a specific budget, filtered by userId
    @Query("SELECT * FROM transactions WHERE budgetId = :budgetId AND userId = :userId ORDER BY date DESC")
    fun getTransactionsByBudgetId(budgetId: Int, userId: String): Flow<List<Transactions>>

    @Update
    suspend fun update(transactions: Transactions)

    @Delete
    suspend fun delete(transactions: Transactions)
}