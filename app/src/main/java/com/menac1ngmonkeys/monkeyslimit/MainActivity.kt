package com.menac1ngmonkeys.monkeyslimit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.AppFAB
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.*
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.BottomBar
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavGraph
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.TopBar
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.DialogItem
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.TransactionDialog
import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase
import com.menac1ngmonkeys.monkeyslimit.data.local.seeders.SeedCoordinator

import com.menac1ngmonkeys.monkeyslimit.utils.navigateToTopLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // awal test database
        // Jalankan dev seeder hanya saat debug build untuk mengisi data dummy FE
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@MainActivity)
                SeedCoordinator.seedDev(db)
        }
//        lifecycleScope.launch {
//            val appContainer = (application as MonkeyslimitApplication).container
//
//            // When id is set to autoGenerate = true, we pass 0. Room will generate a new ID.
//
//            // Test Budgets
//            val budgetsRepository = appContainer.budgetsRepository
//            val newBudget = Budgets(id = 0, name = "Monthly Budget", amount = 500.0, limitAmount = 1000.0, startDate = Date(), endDate = null, note = "Monthly expense budget")
//            val budgetId = budgetsRepository.insert(newBudget).toInt()
//            Log.d("DB_TEST", "Inserted Budget ID: $budgetId")
//            val budgetToUpdate = budgetsRepository.getBudgetById(budgetId).first()
//            budgetsRepository.update(budgetToUpdate.copy(limitAmount = 1200.0))
//            Log.d("DB_TEST", "Updated Budget: ${budgetsRepository.getBudgetById(budgetId).first()}")
//
//            // Test Categories // INI DIHILANGKAN KARENA UDAH ADA SEEDERS
//
//            // Test Transactions
//            val transactionsRepository = appContainer.transactionsRepository
//            val newTransaction = Transactions(id = 0, date = Date(), totalAmount = 50.0, budgetId = budgetId, categoryId = categoryId, note = "Lunch", imagePath = null)
//            val transactionId = transactionsRepository.insert(newTransaction).toInt()
//            Log.d("DB_TEST", "Inserted Transaction ID: $transactionId")
//            val transactionToUpdate = transactionsRepository.getTransactionById(transactionId).first()
//            transactionsRepository.update(transactionToUpdate.copy(totalAmount = 55.0))
//            Log.d("DB_TEST", "Updated Transaction: ${transactionsRepository.getTransactionById(transactionId).first()}")
//
//            // Test SmartSplits
//            val smartSplitsRepository = appContainer.smartSplitsRepository
//            val newSmartSplit = SmartSplits(id = 0, amountOwed = 25.0, createDate = Date())
//            val smartSplitId = smartSplitsRepository.insert(newSmartSplit).toInt()
//            Log.d("DB_TEST", "Inserted SmartSplit ID: $smartSplitId")
//            val smartSplitToUpdate = smartSplitsRepository.getSmartSplitById(smartSplitId).first()
//            smartSplitsRepository.update(smartSplitToUpdate.copy(isPaid = true))
//            Log.d("DB_TEST", "Updated SmartSplit: ${smartSplitsRepository.getSmartSplitById(smartSplitId).first()}")
//
//            // Test Items
//            val itemsRepository = appContainer.itemsRepository
//            val newItem = Items(id = 0, smartSplitId = smartSplitId, name = "Test Item", quantity = 1, totalPrice = 100.0)
//            val itemId = itemsRepository.insert(newItem).toInt()
//            Log.d("DB_TEST", "Inserted Item ID: $itemId")
//            val itemToUpdate = itemsRepository.getItemById(itemId).first()
//            itemsRepository.update(itemToUpdate.copy(quantity = 2))
//            Log.d("DB_TEST", "Updated Item: ${itemsRepository.getItemById(itemId).first()}")
//
//            // Test Members
//            val membersRepository = appContainer.membersRepository
//            val newMember = Members(id = 0, smartSplitId = smartSplitId, name = "John Doe", contact = "555-1234", note = "Friend")
//            val memberId = membersRepository.insert(newMember).toInt()
//            Log.d("DB_TEST", "Inserted Member ID: $memberId")
//            val memberToUpdate = membersRepository.getMemberById(memberId).first()
//            membersRepository.update(memberToUpdate.copy(name = "Jane Doe"))
//            Log.d("DB_TEST", "Updated Member: ${membersRepository.getMemberById(memberId).first()}")
//
//            // Test MemberItems
//            val memberItemsRepository = appContainer.memberItemsRepository
//            val newMemberItem = MemberItems(id = 0, memberId = memberId, itemId = itemId, price = 50.0, quantity = 1)
//            val memberItemId = memberItemsRepository.insert(newMemberItem).toInt()
//            Log.d("DB_TEST", "Inserted MemberItem ID: $memberItemId")
//            // Note: MemberItems doesn't have a standard update method, skipping update test.
//
//            // Test Notifications
//            val notificationsRepository = appContainer.notificationsRepository
//            val newNotification = Notifications(id = 0, title = "Test Notification", description = "This is a test", date = Date())
//            val notificationId = notificationsRepository.insert(newNotification).toInt()
//            Log.d("DB_TEST", "Inserted Notification ID: $notificationId")
//            val notificationToUpdate = notificationsRepository.getNotificationById(notificationId).first()
//            notificationsRepository.update(notificationToUpdate.copy(isCompleted = true))
//            Log.d("DB_TEST", "Updated Notification: ${notificationsRepository.getNotificationById(notificationId).first()}")
//
//        }
        // akhir test database

        setContent {
            MonkeyslimitTheme {
                    MonkeysLimitApp()
            }
        }
    }
}

@Composable
fun MonkeysLimitApp() {
    val navController = rememberNavController()
    val navItems = NavItem.bottomNavItems
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val topBarTitle = when (currentRoute) {
        NavItem.Dashboard.route, null -> "Hi, Welcome"
        else -> navItems.firstOrNull { it.route == currentRoute }?.title ?: "Hi, Welcome"
    }
    var showTransactionDialog by remember { mutableStateOf(false) }
    // Create a list of routes that should NOT have a bottom bar.
    // This is scalable - if you add a new DialogItem, it's automatically included.
    val routesWithoutBottomBar = remember { DialogItem.DialogItems.map { it.route } }
    val showNavElements = currentRoute !in routesWithoutBottomBar

    if (showTransactionDialog) {
        TransactionDialog(
            onDismiss = { showTransactionDialog = false },
            onItemClick = { item ->
                showTransactionDialog = false
                navController.navigate(item.route)
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showNavElements) {
                TopBar(
                    title = topBarTitle,
                    currentRoute = currentRoute,
                    onSettingsClick = {
                        navController.navigateToTopLevel(NavItem.Settings.route)
                    }
                )
            }
        },
        floatingActionButton = {
            if (showNavElements) {
                AppFAB(
                    onClick = { showTransactionDialog = true }
                )
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        bottomBar = {
            if (showNavElements) {
                BottomBar(navController, navItems, currentRoute)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MonkeysLimitAppPreview() {
    MonkeysLimitApp()
}
