package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.SmartSplitDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplit
import kotlinx.coroutines.flow.Flow

class SmartSplitRepository(private val smartSplitDao: SmartSplitDao) {

    fun getAllSmartSplits(): Flow<List<SmartSplit>> = smartSplitDao.getAllSmartSplits()

    fun getSmartSplitById(id: Int): Flow<SmartSplit> = smartSplitDao.getSmartSplitById(id)

    suspend fun insert(smartSplit: SmartSplit) {
        smartSplitDao.insert(smartSplit)
    }

    suspend fun update(smartSplit: SmartSplit) {
        smartSplitDao.update(smartSplit)
    }

    suspend fun delete(smartSplit: SmartSplit) {
        smartSplitDao.delete(smartSplit)
    }
}
