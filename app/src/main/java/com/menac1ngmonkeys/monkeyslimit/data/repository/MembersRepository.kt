package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MembersDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import kotlinx.coroutines.flow.Flow

class MembersRepository(private val membersDao: MembersDao) {

    fun getAllMembers(): Flow<List<Members>> = membersDao.getAllMembers()

    fun getMemberById(id: Int): Flow<Members?> = membersDao.getMemberById(id)

    suspend fun insert(members: Members): Long {
        return membersDao.insert(members)
    }

    suspend fun update(members: Members) {
        membersDao.update(members)
    }

    suspend fun delete(members: Members) {
        membersDao.delete(members)
    }
}
