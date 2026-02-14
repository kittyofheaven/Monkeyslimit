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

    @Query("SELECT * FROM members WHERE userId = :userId")
    fun getAllMembers(userId: String): Flow<List<Members>>

    @Query("SELECT * FROM members WHERE smartSplitId = :smartSplitId AND userId = :userId")
    suspend fun getMembersBySmartSplitNow(smartSplitId: Int, userId: String): List<Members>

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int

    @Query("SELECT * FROM members WHERE id = :id AND userId = :userId")
    fun getMemberById(id: Int, userId: String): Flow<Members?>

    @Query("SELECT * FROM members WHERE smartSplitId IS NULL AND userId = :userId ORDER BY name ASC")
    fun getAllGlobalContacts(userId: String): Flow<List<Members>>

    @Query("SELECT * FROM members WHERE smartSplitId = :splitId AND userId = :userId")
    fun getMembersBySplitId(splitId: Int, userId: String): Flow<List<Members>>

    @Update
    suspend fun update(members: Members)

    @Delete
    suspend fun delete(members: Members)
}