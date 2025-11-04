package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.ItemDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    fun getAllItems(): Flow<List<Item>> = itemDao.getAllItems()

    fun getItemById(id: Int): Flow<Item> = itemDao.getItemById(id)

    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }

    suspend fun update(item: Item) {
        itemDao.update(item)
    }

    suspend fun delete(item: Item) {
        itemDao.delete(item)
    }
}
