package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplits
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartSplitsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smartSplits: SmartSplits): Long

    @Query("SELECT * FROM smartsplits")
    fun getAllSmartSplits(): Flow<List<SmartSplits>>

    @Query("SELECT * FROM smartsplits WHERE id = :id")
    fun getSmartSplitById(id: Int): Flow<SmartSplits?>

    @Update
    suspend fun update(smartSplits: SmartSplits)

    @Delete
    suspend fun delete(smartSplits: SmartSplits)
}
