package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.SmartSplitsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplits
import kotlinx.coroutines.flow.Flow

class SmartSplitsRepository(private val smartSplitsDao: SmartSplitsDao) {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getAllSmartSplits(): Flow<List<SmartSplits>> = smartSplitsDao.getAllSmartSplits(currentUserId)

    fun getSmartSplitById(id: Int): Flow<SmartSplits?> = smartSplitsDao.getSmartSplitById(id, currentUserId)

    suspend fun insert(smartSplits: SmartSplits): Long {
        return smartSplitsDao.insert(smartSplits.copy(userId = currentUserId))
    }

    suspend fun update(smartSplits: SmartSplits) {
        smartSplitsDao.update(smartSplits.copy(userId = currentUserId))
    }

    suspend fun delete(smartSplits: SmartSplits) {
        smartSplitsDao.delete(smartSplits)
    }
}