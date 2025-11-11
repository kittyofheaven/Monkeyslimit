package com.menac1ngmonkeys.monkeyslimit.data

import android.content.Context
import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase
import com.menac1ngmonkeys.monkeyslimit.data.local.seeders.CategoriesSeeder
import com.menac1ngmonkeys.monkeyslimit.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface AppContainer {
    val budgetsRepository: BudgetsRepository
    val categoriesRepository: CategoriesRepository
    val itemsRepository: ItemsRepository
    val membersRepository: MembersRepository
    val memberItemsRepository: MemberItemsRepository
    val notificationsRepository: NotificationsRepository
    val smartSplitsRepository: SmartSplitsRepository
    val transactionsRepository: TransactionsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database by lazy {
        AppDatabase.getDatabase(context)
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Ensure core seeds (Categories) are present for all users
        applicationScope.launch {
            com.menac1ngmonkeys.monkeyslimit.data.local.seeders.SeedCoordinator.seedCore(database)
        }
    }

    override val budgetsRepository: BudgetsRepository by lazy {
        BudgetsRepository(database.budgetsDao())
    }

    override val categoriesRepository: CategoriesRepository by lazy {
        CategoriesRepository(database.categoriesDao())
    }

    override val itemsRepository: ItemsRepository by lazy {
        ItemsRepository(database.itemsDao())
    }

    override val membersRepository: MembersRepository by lazy {
        MembersRepository(database.membersDao())
    }

    override val memberItemsRepository: MemberItemsRepository by lazy {
        MemberItemsRepository(database.memberItemsDao())
    }

    override val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(database.notificationsDao())
    }

    override val smartSplitsRepository: SmartSplitsRepository by lazy {
        SmartSplitsRepository(database.smartSplitsDao())
    }

    override val transactionsRepository: TransactionsRepository by lazy {
        TransactionsRepository(database.transactionsDao())
    }
}
