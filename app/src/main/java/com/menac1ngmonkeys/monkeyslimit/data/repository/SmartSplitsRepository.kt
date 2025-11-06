package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.SmartSplitsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplits
import kotlinx.coroutines.flow.Flow

class SmartSplitsRepository(private val smartSplitsDao: SmartSplitsDao) {

    fun getAllSmartSplits(): Flow<List<SmartSplits>> = smartSplitsDao.getAllSmartSplits()

    fun getSmartSplitById(id: Int): Flow<SmartSplits?> = smartSplitsDao.getSmartSplitById(id)

    suspend fun insert(smartSplits: SmartSplits): Long {
        return smartSplitsDao.insert(smartSplits)
    }

    suspend fun update(smartSplits: SmartSplits) {
        smartSplitsDao.update(smartSplits)
    }

    suspend fun delete(smartSplits: SmartSplits) {
        smartSplitsDao.delete(smartSplits)
    }
}
