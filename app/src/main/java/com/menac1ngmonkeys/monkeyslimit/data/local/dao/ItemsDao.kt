package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Items
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: Items): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Items>)

    @Query("SELECT * FROM items WHERE userId = :userId")
    fun getAllItems(userId: String): Flow<List<Items>>

    @Query("SELECT * FROM items WHERE smartSplitId = :smartSplitId AND userId = :userId")
    suspend fun getItemsBySmartSplitNow(smartSplitId: Int, userId: String): List<Items>

    @Query("SELECT * FROM items WHERE smartSplitId = :smartSplitId AND userId = :userId")
    fun getItemsBySmartSplitId(smartSplitId: Int, userId: String): Flow<List<Items>>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun count(): Int

    @Query("SELECT * FROM items WHERE id = :id AND userId = :userId")
    fun getItemById(id: Int, userId: String): Flow<Items?>

    @Update
    suspend fun update(items: Items)

    @Delete
    suspend fun delete(items: Items)
}