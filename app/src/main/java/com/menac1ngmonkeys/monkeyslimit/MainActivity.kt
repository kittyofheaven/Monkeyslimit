package com.menac1ngmonkeys.monkeyslimit

import android.os.Bundle
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.AppFAB
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.BottomBar
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavGraph
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.TopBar
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.DialogItem
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.TransactionDialog

import com.menac1ngmonkeys.monkeyslimit.utils.navigateToTopLevel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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