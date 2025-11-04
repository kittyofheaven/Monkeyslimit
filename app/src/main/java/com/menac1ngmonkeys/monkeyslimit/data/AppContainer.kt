package com.menac1ngmonkeys.monkeyslimit.data

import android.content.Context
import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase
import com.menac1ngmonkeys.monkeyslimit.data.repository.*

interface AppContainer {
    val budgetRepository: BudgetRepository
    val categoryRepository: CategoryRepository
    val itemRepository: ItemRepository
    val memberRepository: MemberRepository
    val memberItemsRepository: MemberItemsRepository
    val notificationRepository: NotificationRepository
    val smartSplitRepository: SmartSplitRepository
    val transactionRepository: TransactionRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database by lazy {
        AppDatabase.getDatabase(context)
    }

    override val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(database.budgetDao())
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    override val itemRepository: ItemRepository by lazy {
        ItemRepository(database.itemDao())
    }

    override val memberRepository: MemberRepository by lazy {
        MemberRepository(database.memberDao())
    }

    override val memberItemsRepository: MemberItemsRepository by lazy {
        MemberItemsRepository(database.memberItemsDao())
    }

    override val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(database.notificationDao())
    }

    override val smartSplitRepository: SmartSplitRepository by lazy {
        SmartSplitRepository(database.smartSplitDao())
    }

    override val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }
}
