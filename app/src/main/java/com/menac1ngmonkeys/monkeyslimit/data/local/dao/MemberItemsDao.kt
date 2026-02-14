package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.MemberItems
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memberItems: MemberItems): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memberItems: List<MemberItems>)

    @Query("SELECT * FROM memberitems WHERE userId = :userId")
    fun getAllMemberItems(userId: String): Flow<List<MemberItems>>

    @Query("SELECT * FROM memberitems WHERE memberId = :memberId AND userId = :userId")
    fun getMemberItemsByMemberId(memberId: Int, userId: String): Flow<List<MemberItems>>

    @Query("SELECT COUNT(*) FROM memberitems")
    suspend fun count(): Int

    @Delete
    suspend fun delete(memberItems: MemberItems)
}