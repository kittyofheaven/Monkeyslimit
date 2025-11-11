package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import kotlinx.coroutines.flow.Flow

@Dao
interface MembersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(members: Members): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<Members>)

    @Query("SELECT * FROM members")
    fun getAllMembers(): Flow<List<Members>>

    @Query("SELECT * FROM members WHERE smartSplitId = :smartSplitId")
    suspend fun getMembersBySmartSplitNow(smartSplitId: Int): List<Members>

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Int): Flow<Members?>

    @Update
    suspend fun update(members: Members)

    @Delete
    suspend fun delete(members: Members)
}
