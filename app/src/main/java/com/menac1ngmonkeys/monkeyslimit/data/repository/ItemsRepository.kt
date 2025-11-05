package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.ItemsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Items
import kotlinx.coroutines.flow.Flow

class ItemsRepository(private val itemsDao: ItemsDao) {

    fun getAllItems(): Flow<List<Items>> = itemsDao.getAllItems()

    fun getItemById(id: Int): Flow<Items> = itemsDao.getItemById(id)

    suspend fun insert(items: Items): Long {
        return itemsDao.insert(items)
    }

    suspend fun update(items: Items) {
        itemsDao.update(items)
    }

    suspend fun delete(items: Items) {
        itemsDao.delete(items)
    }
}
