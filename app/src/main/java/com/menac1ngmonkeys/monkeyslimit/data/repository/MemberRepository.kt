package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MemberDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Member
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val memberDao: MemberDao) {

    fun getAllMembers(): Flow<List<Member>> = memberDao.getAllMembers()

    fun getMemberById(id: Int): Flow<Member> = memberDao.getMemberById(id)

    suspend fun insert(member: Member) {
        memberDao.insert(member)
    }

    suspend fun update(member: Member) {
        memberDao.update(member)
    }

    suspend fun delete(member: Member) {
        memberDao.delete(member)
    }
}
