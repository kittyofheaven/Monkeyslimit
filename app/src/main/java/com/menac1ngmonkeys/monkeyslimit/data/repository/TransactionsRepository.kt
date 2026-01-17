package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.TransactionsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import kotlinx.coroutines.flow.Flow

class TransactionsRepository(private val transactionsDao: TransactionsDao) {

    fun getAllTransactions(): Flow<List<Transactions>> = transactionsDao.getAllTransactions()

    fun getTransactionById(id: Int): Flow<Transactions?> = transactionsDao.getTransactionById(id)

    // Get all transactions for a specific budget
    fun getTransactionsByBudgetId(budgetId: Int): Flow<List<Transactions>> {
        return transactionsDao.getTransactionsByBudgetId(budgetId)
    }

    suspend fun insert(transactions: Transactions): Long {
        return transactionsDao.insert(transactions)
    }

    suspend fun update(transactions: Transactions) {
        transactionsDao.update(transactions)
    }

    suspend fun delete(transactions: Transactions) {
        transactionsDao.delete(transactions)
    }
}
