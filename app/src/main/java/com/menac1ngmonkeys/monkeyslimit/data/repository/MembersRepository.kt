package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MembersDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import kotlinx.coroutines.flow.Flow

class MembersRepository(private val membersDao: MembersDao) {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getAllMembers(): Flow<List<Members>> = membersDao.getAllMembers(currentUserId)

    fun getMemberById(id: Int): Flow<Members?> = membersDao.getMemberById(id, currentUserId)

    fun getAllGlobalContacts(): Flow<List<Members>> = membersDao.getAllGlobalContacts(currentUserId)

    fun getMembersBySplitId(splitId: Int): Flow<List<Members>> = membersDao.getMembersBySplitId(splitId, currentUserId)

    suspend fun insert(members: Members): Long {
        return membersDao.insert(members.copy(userId = currentUserId))
    }

    suspend fun update(members: Members) {
        membersDao.update(members.copy(userId = currentUserId))
    }

    suspend fun delete(members: Members) {
        membersDao.delete(members)
    }
}