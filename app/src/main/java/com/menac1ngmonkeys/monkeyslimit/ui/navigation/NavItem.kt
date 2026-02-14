package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import com.menac1ngmonkeys.monkeyslimit.R

sealed class NavItem (
    val route: String,
    val title: String,
    val iconId: Int,
    val showBottomBar: Boolean = false,
    val showTopBar: Boolean = false,
) {
    object Analytics : NavItem(
        route = "analytics",
        title = "Analytics",
        iconId = R.drawable.analytics_40px,
        showBottomBar = true,
        showTopBar = true
    )

    object Budget : NavItem(
        route = "budget",
        title = "Budget",
        iconId = R.drawable.budget_40px,
        showBottomBar = true,
        showTopBar = true
    )

    object AddBudget : NavItem(
        route = "add_budget",
        title = "Add Budget",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    object BudgetRecommendation : NavItem(
        route = "budget_recommendation",
        title = "AI Plan",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    object Dashboard : NavItem(
        route = "dashboard",
        title = "Dashboard",
        iconId = R.drawable.home_40px,
        showBottomBar = true,
        showTopBar = true
    )

    object Settings : NavItem(
        route = "settings",
        title = "Settings",
        iconId = R.drawable.settings_24px,
        showBottomBar = false,
        showTopBar = true
    )

    object Profile : NavItem(
        route = "profile",
        title = "Profile",
        iconId = R.drawable.account_circle_24dp,
        showBottomBar = false,
        showTopBar = true
    )

    object EditProfile : NavItem(
        route = "edit_profile",
        title = "Edit Profile",
        iconId = 0,
        showBottomBar = false,
        showTopBar = false
    )

    object ImagePreview : NavItem(
        route = "image_preview/{encodedUri}",
        title = "Preview Image",
        iconId = 0,
        showBottomBar = false,
        showTopBar = false // We'll build a custom top bar in the screen
    ) {
        // Helper to create the route with the argument
        fun createRoute(encodedUri: String) = "image_preview/$encodedUri"
    }

    object CompleteProfile : NavItem(
        route = "complete_profile",
        title = "Complete Profile",
        iconId = 0,
        showBottomBar = false,
        showTopBar = false
    )

    object SmartSplit : NavItem(
        route = "smart_split",
        title = "Split Bill",
        iconId = R.drawable.smart_split_40px,
        showBottomBar = false,
        showTopBar = true
    )

    object ReviewSmartSplit : NavItem(
        route = "review_smart_split/{imageUri}",
        title = "Review Split Bill",
        iconId = R.drawable.smart_split_40px,
        showBottomBar = false,
        showTopBar = true
    )

    object SelectMember : NavItem(
        route = "select_member?exclude={exclude}",
        title = "Select Member",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    object SplitResult : NavItem(
        route = "split_result_screen",
        title = "Split Result",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    object GallerySplit : NavItem(
        route = "gallery_split",
        title = "Gallery Split",
        iconId = R.drawable.image_40px,
        showBottomBar = false,
        showTopBar = true
    )

    object ManualSplit : NavItem(
        route = "manual_split",
        title = "Manual Split",
        iconId = R.drawable.manual_edit_40dp,
        showBottomBar = false,
        showTopBar = true
    )

    object SmartSplitHistory : NavItem(
        route = "smart_split_history",
        title = "History",
        iconId = 0, // Assuming you have an icon, or use 0
        showBottomBar = false,
        showTopBar = true
    )

    object SmartSplitDetail : NavItem(
        route = "smart_split_detail/{splitId}",
        title = "Split Detail",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    object Transaction : NavItem(
        route = "transaction",
        title = "Transaction",
        iconId = R.drawable.add_1_40dp,
        showBottomBar = false,
        showTopBar = true
    )

    object TransactionDetail : NavItem(
        route = "transaction_detail/{transactionId}",
        title = "Transaction Detail",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    object ScanTransaction : NavItem(
        route = "scan_transaction",
        title = "Scan Transaction",
        iconId = R.drawable.camera_40px,
        showBottomBar = false,
        showTopBar = true
    )

    object GalleryTransaction : NavItem(
        route = "gallery_transaction",
        title = "Gallery Transaction",
        iconId = R.drawable.image_40px,
        showBottomBar = false,
        showTopBar = true
    )

    object ManualTransaction : NavItem(
        route = "manual_transaction",
        title = "Manual Transaction",
        iconId = R.drawable.manual_edit_40dp,
        showBottomBar = false,
        showTopBar = true
    )

    object ReviewTransaction : NavItem(
        // UPDATE THIS LINE:
        route = "review_transaction/{imageUri}?ocrText={ocrText}",
        title = "Review Transaction",
        iconId = 0,
        showBottomBar = false,
        showTopBar = true
    )

    companion object {
        val mainNavItems = listOf(
            // Main screens
            Dashboard,
            Budget,
            Transaction,
            Analytics,
            SmartSplit,
        )

        val subNavItems = listOf(
            // Sub screens in the main screens
            AddBudget,
            Settings,
            Profile,
            EditProfile,
            ScanTransaction,
            GalleryTransaction,
            ManualTransaction,
            ReviewTransaction,
            ReviewSmartSplit,
            SelectMember,
            SplitResult,
            SmartSplitHistory,
            SmartSplitDetail,
            BudgetRecommendation
        )

        val FABMenu = listOf(
            ScanTransaction,
            ManualTransaction,
        )
    }
}
