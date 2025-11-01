package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import com.menac1ngmonkeys.monkeyslimit.ui.analytics.AnalyticsScreen
import com.menac1ngmonkeys.monkeyslimit.ui.budget.BudgetScreen
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardScreen
import com.menac1ngmonkeys.monkeyslimit.ui.settings.SettingsScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SmartSplitScreen

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
    }
}