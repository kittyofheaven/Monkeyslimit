package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.TransactionsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import kotlinx.coroutines.flow.Flow

class TransactionsRepository(private val transactionsDao: TransactionsDao) {

    // 1. Automatically grab the current logged-in User ID
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // 2. Pass the userId to your read queries
    fun getAllTransactions(): Flow<List<Transactions>> =
        transactionsDao.getAllTransactions(currentUserId)

    fun getTransactionsByType(type: TransactionType): Flow<List<Transactions>> =
        transactionsDao.getTransactionsByType(type, currentUserId)

    fun getTransactionById(id: Int): Flow<Transactions?> =
        transactionsDao.getTransactionById(id, currentUserId)

    fun getTransactionsByBudgetId(budgetId: Int): Flow<List<Transactions>> {
        return transactionsDao.getTransactionsByBudgetId(budgetId, currentUserId)
    }

    // 3. Intercept and inject the userId before saving/updating
    suspend fun insert(transactions: Transactions): Long {
        return transactionsDao.insert(transactions.copy(userId = currentUserId))
    }

    suspend fun update(transactions: Transactions) {
        transactionsDao.update(transactions.copy(userId = currentUserId))
    }

    suspend fun delete(transactions: Transactions) {
        transactionsDao.delete(transactions)
    }

    fun getTotalIncome(): Flow<Double?> = transactionsDao.getTotalIncome(currentUserId)
    fun getTotalExpense(): Flow<Double?> = transactionsDao.getTotalExpense(currentUserId)
}