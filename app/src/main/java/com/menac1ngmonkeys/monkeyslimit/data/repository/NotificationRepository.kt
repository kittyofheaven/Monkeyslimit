package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.NotificationDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Notification
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun getAllNotifications(): Flow<List<Notification>> = notificationDao.getAllNotifications()

    fun getNotificationById(id: Int): Flow<Notification> = notificationDao.getNotificationById(id)

    suspend fun insert(notification: Notification) {
        notificationDao.insert(notification)
    }

    suspend fun update(notification: Notification) {
        notificationDao.update(notification)
    }

    suspend fun delete(notification: Notification) {
        notificationDao.delete(notification)
    }
}
