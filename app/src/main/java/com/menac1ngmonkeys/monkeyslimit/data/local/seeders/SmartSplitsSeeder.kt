package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.SmartSplitsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplits

object SmartSplitsSeeder {
    suspend fun seed(smartSplitsDao: SmartSplitsDao): List<Int> {
        if (smartSplitsDao.count() > 0) return emptyList()
        val s1 = smartSplitsDao.insert(SmartSplits(id = 0, amountOwed = 1_740_000.0, createDate = SeedUtils.daysAgo(1))).toInt()
        val s2 = smartSplitsDao.insert(SmartSplits(id = 0, amountOwed = 320_000.0, createDate = SeedUtils.daysAgo(0))).toInt()
        return listOf(s1, s2)
    }
}

