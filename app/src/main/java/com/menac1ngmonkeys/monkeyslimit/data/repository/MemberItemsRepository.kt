package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MemberItemsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.MemberItems
import kotlinx.coroutines.flow.Flow

class MemberItemsRepository(private val memberItemsDao: MemberItemsDao) {

    fun getAllMemberItems(): Flow<List<MemberItems>> = memberItemsDao.getAllMemberItems()

    fun getMemberItemsByMemberId(memberId: Int): Flow<List<MemberItems>> = memberItemsDao.getMemberItemsByMemberId(memberId)

    suspend fun insert(memberItems: MemberItems): Long {
        return memberItemsDao.insert(memberItems)
    }

    suspend fun delete(memberItems: MemberItems) {
        memberItemsDao.delete(memberItems)
    }
}
