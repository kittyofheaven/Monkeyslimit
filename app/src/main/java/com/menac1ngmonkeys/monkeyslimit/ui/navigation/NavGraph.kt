package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import com.menac1ngmonkeys.monkeyslimit.ui.analytics.AnalyticsScreen
import com.menac1ngmonkeys.monkeyslimit.ui.budget.BudgetScreen
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardScreen
import com.menac1ngmonkeys.monkeyslimit.ui.settings.SettingsScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SmartSplitScreen
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.DialogItem

/**
 * Sets up the **main navigation graph** for the Monkeyslimit app.
 *
 * This composable defines the top-level destinations for the application's
 * navigation flow using a [NavHost]. Each route corresponds to a primary screen
 * (Dashboard, Budget, SmartSplit, Analytics, Settings).
 *
 * @param navController The [NavHostController] used to control navigation
 * between destinations.
 * @param modifier Optional [Modifier] for customizing layout or padding behavior.
 *
 * ### Destinations:
 * - **Dashboard** — The home screen showing the main overview.
 * - **Budget** — The page for managing expenses and income.
 * - **SmartSplit** — The screen for bill-splitting features.
 * - **Analytics** — The financial insights and charts view.
 * - **Settings** — The configuration and preferences screen.
 *
 * ### Example usage:
 * ```
 * val navController = rememberNavController()
 *
 * Scaffold(
 *     bottomBar = { BottomBar(navController, navItems, currentRoute) }
 * ) { innerPadding ->
 *     NavGraph(
 *         navController = navController,
 *         modifier = Modifier.padding(innerPadding)
 *     )
 * }
 * ```
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavItem.Dashboard.route,
        modifier = modifier
    ) {
        composable(NavItem.Dashboard.route) {
            DashboardScreen()
        }
        composable(NavItem.Budget.route) {
            BudgetScreen()
        }
        composable(NavItem.SmartSplit.route) {
            SmartSplitScreen()
        }
        composable(NavItem.Analytics.route) {
            AnalyticsScreen()
        }
        composable(NavItem.Settings.route) {
            SettingsScreen()
        }
        composable(DialogItem.Gallery.route) {
            // Temporary Gallery Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Gallery Screen")
                Button(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Text("Go Back")
                }
            }
        }
        composable(DialogItem.Camera.route) {
            // Temporary Camera Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Camera Screen")
                Button(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Text("Go Back")
                }
            }
        }
        composable(DialogItem.AI.route) {
            // Temporary AI Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("AI Screen")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            navController.navigate(DialogItem.Camera.route)
                        }
                    ) {
                        Text("Camera")
                    }
                    Button(
                        onClick = {
                            navController.navigate(DialogItem.Gallery.route)
                        }
                    ) {
                        Text("Gallery")
                    }
                }
                Button(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Text("Go Back")
                }
            }
        }
        composable(DialogItem.Manual.route) {
            // Temporary Manual Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Manual Screen")
                Button(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}