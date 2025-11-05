package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.NotificationsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Notifications
import kotlinx.coroutines.flow.Flow

class NotificationsRepository(private val notificationsDao: NotificationsDao) {

    fun getAllNotifications(): Flow<List<Notifications>> = notificationsDao.getAllNotifications()

    fun getNotificationById(id: Int): Flow<Notifications> = notificationsDao.getNotificationById(id)

    suspend fun insert(notifications: Notifications): Long {
        return notificationsDao.insert(notifications)
    }

    suspend fun update(notifications: Notifications) {
        notificationsDao.update(notifications)
    }

    suspend fun delete(notifications: Notifications) {
        notificationsDao.delete(notifications)
    }
}
