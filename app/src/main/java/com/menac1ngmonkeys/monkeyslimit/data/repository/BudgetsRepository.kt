package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.BudgetsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import kotlinx.coroutines.flow.Flow

class BudgetsRepository(private val budgetsDao: BudgetsDao) {

    // 1. Automatically grab the current logged-in User ID
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // 2. Pass the userId to your read queries
    fun getAllBudgets(): Flow<List<Budgets>> = budgetsDao.getAllBudgets(currentUserId)

    fun getBudgetById(id: Int): Flow<Budgets?> = budgetsDao.getBudgetById(id, currentUserId)

    // 3. Intercept and inject the userId before saving/updating
    suspend fun insert(budgets: Budgets): Long {
        return budgetsDao.insert(budgets.copy(userId = currentUserId))
    }

    suspend fun update(budgets: Budgets) {
        budgetsDao.update(budgets.copy(userId = currentUserId))
    }

    suspend fun delete(budgets: Budgets) {
        budgetsDao.delete(budgets) // Primary key is enough to delete, no need to copy
    }
}