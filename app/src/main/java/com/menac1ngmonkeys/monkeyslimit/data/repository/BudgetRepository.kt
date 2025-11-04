package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetById(id: Int): Flow<Budget> = budgetDao.getBudgetById(id)

    suspend fun insert(budget: Budget) {
        budgetDao.insert(budget)
    }

    suspend fun update(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun delete(budget: Budget) {
        budgetDao.delete(budget)
    }
}
