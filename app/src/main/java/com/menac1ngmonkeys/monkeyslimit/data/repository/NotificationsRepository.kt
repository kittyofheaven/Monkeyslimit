package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.NotificationsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Notifications
import kotlinx.coroutines.flow.Flow

class NotificationsRepository(private val notificationsDao: NotificationsDao) {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getAllNotifications(): Flow<List<Notifications>> = notificationsDao.getAllNotifications(currentUserId)

    fun getNotificationById(id: Int): Flow<Notifications?> = notificationsDao.getNotificationById(id, currentUserId)

    suspend fun insert(notifications: Notifications): Long {
        return notificationsDao.insert(notifications.copy(userId = currentUserId))
    }

    suspend fun update(notifications: Notifications) {
        notificationsDao.update(notifications.copy(userId = currentUserId))
    }

    suspend fun delete(notifications: Notifications) {
        notificationsDao.delete(notifications)
    }
}