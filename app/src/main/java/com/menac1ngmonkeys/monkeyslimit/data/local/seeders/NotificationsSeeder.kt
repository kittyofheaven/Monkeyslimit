package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.NotificationsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Notifications

object NotificationsSeeder {
    suspend fun seed(notificationsDao: NotificationsDao) {
        if (notificationsDao.count() > 0) return
        val notes = listOf(
            Notifications(id = 0, title = "Bayar tagihan internet", description = "Jatuh tempo minggu ini", date = SeedUtils.daysFromNow(2), isCompleted = false),
            Notifications(id = 0, title = "Review anggaran", description = "Cek pengeluaran bulan ini", date = SeedUtils.daysFromNow(0), isCompleted = false),
            Notifications(id = 0, title = "Membership gym", description = "Perpanjang 7 hari lagi", date = SeedUtils.daysFromNow(7), isCompleted = false)
        )
        notificationsDao.insertAll(notes)
    }
}

