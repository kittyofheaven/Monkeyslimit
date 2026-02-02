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

    // 1. Fetch only Global Contacts (for Selection Screen)
    @Query("SELECT * FROM members WHERE smartSplitId IS NULL ORDER BY name ASC")
    fun getAllGlobalContacts(): Flow<List<Members>>

    // 2. Fetch Members belonging to a specific Bill (for History/Result)
    @Query("SELECT * FROM members WHERE smartSplitId = :splitId")
    fun getMembersBySplitId(splitId: Int): Flow<List<Members>>


    @Update
    suspend fun update(members: Members)

    @Delete
    suspend fun delete(members: Members)
}
