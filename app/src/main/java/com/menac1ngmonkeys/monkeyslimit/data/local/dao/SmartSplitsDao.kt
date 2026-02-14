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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smartSplits: List<SmartSplits>)

    @Query("SELECT * FROM smartsplits WHERE userId = :userId")
    fun getAllSmartSplits(userId: String): Flow<List<SmartSplits>>

    @Query("SELECT COUNT(*) FROM smartsplits")
    suspend fun count(): Int

    @Query("SELECT * FROM smartsplits WHERE id = :id AND userId = :userId")
    fun getSmartSplitById(id: Int, userId: String): Flow<SmartSplits?>

    @Update
    suspend fun update(smartSplits: SmartSplits)

    @Delete
    suspend fun delete(smartSplits: SmartSplits)
}