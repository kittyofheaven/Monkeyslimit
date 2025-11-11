package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MembersDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members

object MembersSeeder {
    suspend fun seed(membersDao: MembersDao, splitIds: List<Int>): Map<Int, Map<String, Int>> {
        val result: MutableMap<Int, Map<String, Int>> = mutableMapOf()
        if (splitIds.isEmpty()) return result

        return if (membersDao.count() == 0) {
            splitIds.forEachIndexed { index, splitId ->
                val names = if (index == 0) listOf("Alice", "Bob", "Charlie") else listOf("Alice", "Bob")
                val map = names.associate { name ->
                    name to membersDao.insert(Members(id = 0, smartSplitId = splitId, name = name, contact = null, note = null)).toInt()
                }
                result[splitId] = map
            }
            result
        } else {
            splitIds.forEach { splitId ->
                val list = membersDao.getMembersBySmartSplitNow(splitId)
                result[splitId] = list.associate { it.name to it.id }
            }
            result
        }
    }
}

