package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import kotlinx.coroutines.flow.Flow

class BudgetsRepository(private val budgetsDao: BudgetsDao) {

    fun getAllBudgets(): Flow<List<Budgets>> = budgetsDao.getAllBudgets()

    fun getBudgetById(id: Int): Flow<Budgets> = budgetsDao.getBudgetById(id)

    suspend fun insert(budgets: Budgets): Long {
        return budgetsDao.insert(budgets)
    }

    suspend fun update(budgets: Budgets) {
        budgetsDao.update(budgets)
    }

    suspend fun delete(budgets: Budgets) {
        budgetsDao.delete(budgets)
    }
}
