package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.ItemsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Items
import kotlinx.coroutines.flow.Flow

class ItemsRepository(private val itemsDao: ItemsDao) {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getAllItems(): Flow<List<Items>> = itemsDao.getAllItems(currentUserId)

    fun getItemById(id: Int): Flow<Items?> = itemsDao.getItemById(id, currentUserId)

    fun getItemsBySplitId(splitId: Int): Flow<List<Items>> = itemsDao.getItemsBySmartSplitId(splitId, currentUserId)

    suspend fun insert(items: Items): Long {
        return itemsDao.insert(items.copy(userId = currentUserId))
    }

    suspend fun update(items: Items) {
        itemsDao.update(items.copy(userId = currentUserId))
    }

    suspend fun delete(items: Items) {
        itemsDao.delete(items)
    }
}