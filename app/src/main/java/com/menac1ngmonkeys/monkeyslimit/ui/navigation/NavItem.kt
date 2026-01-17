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

    object AddBudget : NavItem(
        route = "add_budget",
        title = "Add Budget",
        iconId = 0
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

    object Profile : NavItem(
        route = "profile",
        title = "Profile",
        iconId = R.drawable.account_circle_24dp
    )

    object CompleteProfile : NavItem(
        route = "complete_profile",
        title = "Complete Profile",
        iconId = 0
    )

    object SmartSplit : NavItem(
        route = "smart_split",
        title = "Split Bill",
        iconId = R.drawable.smart_split_40px
    )

    object Transaction : NavItem(
        route = "transaction",
        title = "Transaction",
        iconId = R.drawable.add_1_40dp
    )

    object BudgetDetail : NavItem(
        // The route includes a placeholder for the budgetId
        route = "budget_detail/{budgetId}",
        // You don't need a title or icon if it's not in the bottom bar
        title = "Budget Detail",
        iconId = 0
    ) {
        // Helper function to create the full route with a specific ID
        fun withArgs(budgetId: Int): String {
            return "budget_detail/$budgetId"
        }
    }

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Budget,
            Transaction,
            Analytics,
            SmartSplit,
        )

        val topNavItems = listOf(
            AddBudget,
            Settings,
            Profile,
        )
    }
}
