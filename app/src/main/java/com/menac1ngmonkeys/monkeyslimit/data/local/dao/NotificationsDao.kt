package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Notifications
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notifications: Notifications): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<Notifications>)

    @Query("SELECT * FROM notifications")
    fun getAllNotifications(): Flow<List<Notifications>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Int

    @Query("SELECT * FROM notifications WHERE id = :id")
    fun getNotificationById(id: Int): Flow<Notifications?>

    @Update
    suspend fun update(notifications: Notifications)

    @Delete
    suspend fun delete(notifications: Notifications)
}
