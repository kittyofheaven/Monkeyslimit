package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import com.menac1ngmonkeys.monkeyslimit.R

sealed class NavItem (
    val route: String,
    val title: String,
    val iconId: Int
) {
    object Analytics : NavItem(
        route = "analytics",
        title = "Analytics",
        iconId = R.drawable.analytics_40px
    )

    object Budget : NavItem(
        route = "budget",
        title = "Budget",
        iconId = R.drawable.budget_40px
    )

    object Dashboard : NavItem(
        route = "dashboard",
        title = "Dashboard",
        iconId = R.drawable.home_40px
    )

    object Settings : NavItem(
        route = "settings",
        title = "Settings",
        iconId = R.drawable.settings_24px
    )

    object SmartSplit : NavItem(
        route = "smart_split",
        title = "Split Bill",
        iconId = R.drawable.smart_split_40px
    )

    object Transaction : NavItem(
        route = "transaction",
        title = "Transaction",
        iconId = R.drawable.transactions
    )

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Budget,
            Transaction,
            Analytics,
            SmartSplit
        )
    }
}
