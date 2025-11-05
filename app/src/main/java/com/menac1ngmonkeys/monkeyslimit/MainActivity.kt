package com.menac1ngmonkeys.monkeyslimit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.*
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.BottomBar
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavGraph
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.utils.navigateToTopLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // awal test database
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
//            // Test Categories
//            val categoriesRepository = appContainer.categoriesRepository
//            val newCategory = Categories(id = 0, name = "Food", icon = "restaurant", description = "For food and groceries")
//            val categoryId = categoriesRepository.insert(newCategory).toInt()
//            Log.d("DB_TEST", "Inserted Category ID: $categoryId")
//            val categoryToUpdate = categoriesRepository.getCategoryById(categoryId).first()
//            categoriesRepository.update(categoryToUpdate.copy(name = "Groceries"))
//            Log.d("DB_TEST", "Updated Category: ${categoriesRepository.getCategoryById(categoryId).first()}")
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

@OptIn(ExperimentalMaterial3Api::class) // Topbar still experimental??
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(topBarTitle) },
                actions = {
                    if (currentRoute != NavItem.Settings.route) {
                        IconButton(onClick = {
                            navController.navigateToTopLevel(NavItem.Settings.route)
                        }) {
                            Icon(
                                painter = painterResource(NavItem.Settings.iconId),
                                contentDescription = NavItem.Settings.title
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigateToTopLevel(NavItem.Transaction.route)
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = 50.dp)
                    .border(
                        width = 10.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ) // Disable the shadow
            ) {
                Icon(
                    painter = painterResource(NavItem.Transaction.iconId),
                    contentDescription = NavItem.Transaction.title
                )
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        bottomBar = {
            BottomBar(navController, navItems, currentRoute)
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
