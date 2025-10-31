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