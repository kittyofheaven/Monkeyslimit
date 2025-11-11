package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.ItemsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Items

object ItemsSeeder {
    suspend fun seed(itemsDao: ItemsDao, splitIds: List<Int>): Map<Int, Map<String, Int>> {
        val result: MutableMap<Int, Map<String, Int>> = mutableMapOf()
        if (splitIds.isEmpty()) return result

        return if (itemsDao.count() == 0) {
            splitIds.forEachIndexed { index, splitId ->
                val items = if (index == 0) listOf(
                    Items(id = 0, smartSplitId = splitId, name = "Sushi Platter", quantity = 1, totalPrice = 1_200_000.0),
                    Items(id = 0, smartSplitId = splitId, name = "Drinks", quantity = 3, totalPrice = 300_000.0),
                    Items(id = 0, smartSplitId = splitId, name = "Dessert", quantity = 3, totalPrice = 240_000.0)
                ) else listOf(
                    Items(id = 0, smartSplitId = splitId, name = "Popcorn", quantity = 2, totalPrice = 200_000.0),
                    Items(id = 0, smartSplitId = splitId, name = "Soda", quantity = 2, totalPrice = 120_000.0)
                )
                val map = items.associate { it.name to itemsDao.insert(it).toInt() }
                result[splitId] = map
            }
            result
        } else {
            splitIds.forEach { splitId ->
                val list = itemsDao.getItemsBySmartSplitNow(splitId)
                result[splitId] = list.associate { it.name to it.id }
            }
            result
        }
    }
}

