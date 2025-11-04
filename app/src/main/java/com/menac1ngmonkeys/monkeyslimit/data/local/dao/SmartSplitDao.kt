package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplit
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartSplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smartSplit: SmartSplit)

    @Query("SELECT * FROM smartsplits")
    fun getAllSmartSplits(): Flow<List<SmartSplit>>

    @Query("SELECT * FROM smartsplits WHERE id = :id")
    fun getSmartSplitById(id: Int): Flow<SmartSplit>

    @Update
    suspend fun update(smartSplit: SmartSplit)

    @Delete
    suspend fun delete(smartSplit: SmartSplit)
}
