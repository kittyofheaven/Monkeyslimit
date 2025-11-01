package com.menac1ngmonkeys.monkeyslimit.utils

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Navigate to a top-level destination.
 * - Pops up to the root of the navigation graph
 * - Restores previous state if reselecting
 * - Prevents multiple copies of the same destination
 *
 * @param route The route to navigate to.
 * ### Example usage:
 * ```
 * val navController = rememberNavController()
 *
 * // Navigate to the dashboard tab
 * navController.navigateToTopLevel(NavItem.Dashboard.route)
 *
 * // Navigate to settings
 * navController.navigateToTopLevel(NavItem.Settings.route)
 * ```
 */
fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigate to a destination (not necessarily top-level) in singleTop mode.
 * - Does NOT pop up to the root of the graph
 * - Still avoids duplicate destinations
 * - Good for nested graph navigation or detail screens
 *
 * @param route The route to navigate to.
 * ### Example usage:
 * ```
 * val navController = rememberNavController()
 *
 * // Navigate to a detail screen inside the current graph
 * val itemId = 42
 * navController.navigateSingleTopTo("details/$itemId")
 *
 * // Navigate to a nested screen without popping the current stack
 * navController.navigateSingleTopTo("profile/settings")
 * ```
 */
fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}