package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MemberItemsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.MemberItems
import kotlinx.coroutines.flow.Flow

class MemberItemsRepository(private val memberItemsDao: MemberItemsDao) {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getAllMemberItems(): Flow<List<MemberItems>> = memberItemsDao.getAllMemberItems(currentUserId)

    fun getMemberItemsByMemberId(memberId: Int): Flow<List<MemberItems>> = memberItemsDao.getMemberItemsByMemberId(memberId, currentUserId)

    suspend fun insert(memberItems: MemberItems): Long {
        return memberItemsDao.insert(memberItems.copy(userId = currentUserId))
    }

    suspend fun delete(memberItems: MemberItems) {
        memberItemsDao.delete(memberItems)
    }
}