package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.UserDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UsersRepository(private val userDao: UserDao) {
    fun getUser(uid: String): Flow<User?> = userDao.getUserById(uid)
    suspend fun saveUser(user: User) = userDao.insertUser(user)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}