package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.menac1ngmonkeys.monkeyslimit.ui.analytics.AnalyticsScreen
import com.menac1ngmonkeys.monkeyslimit.ui.auth.CompleteProfileScreen
import com.menac1ngmonkeys.monkeyslimit.ui.budget.AddBudgetScreen
import com.menac1ngmonkeys.monkeyslimit.ui.budget.BudgetScreen
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardScreen
import com.menac1ngmonkeys.monkeyslimit.ui.profile.ProfileScreen
import com.menac1ngmonkeys.monkeyslimit.ui.settings.SettingsScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.ReviewSmartSplitScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SelectMemberScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SmartSplitDetailScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SmartSplitHistoryScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SmartSplitScreen
import com.menac1ngmonkeys.monkeyslimit.ui.smartsplit.SplitResultScreen
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftMember
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.DialogItem
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.ManualTransactionScreen
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.ReviewTransactionScreen
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.ScanTransactionScreen
import com.menac1ngmonkeys.monkeyslimit.utils.navigateSingleTopTo
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ReviewTransactionViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
            DashboardScreen(navController = navController)
        }
        composable(
            route = NavItem.TransactionDetail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: return@composable

            // Import this screen first
            com.menac1ngmonkeys.monkeyslimit.ui.transaction.TransactionDetailScreen(
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavItem.Budget.route) {
            BudgetScreen(navController = navController)
        }
        // ROUTE for the AddBudgetScreen
        composable("add_budget") {
            AddBudgetScreen(navController = navController)
        }
        composable(NavItem.SmartSplit.route) {
            SmartSplitScreen(
                onImagePicked = { uri ->
                    val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
                    // Reference the NavItem and replace the placeholder
                    val route = NavItem.ReviewSmartSplit.route.replace("{imageUri}", encodedUri)
                    navController.navigate(route)
                },
                onHistoryClick = { // Handle the new callback
                    navController.navigate(NavItem.SmartSplitHistory.route)
                }
            )
        }
        // Update History Route to handle click
        composable(NavItem.SmartSplitHistory.route) {
            SmartSplitHistoryScreen(
                onItemClick = { splitId ->
                    navController.navigate("smart_split_detail/$splitId")
                }
            )
        }

        // Add Detail Route
        composable(
            route = "smart_split_detail/{splitId}",
            arguments = listOf(navArgument("splitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val splitId = backStackEntry.arguments?.getInt("splitId") ?: return@composable

            SmartSplitDetailScreen(
                splitId = splitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavItem.ReviewSmartSplit.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            ReviewSmartSplitScreen(
                imageUri = imageUri,
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavItem.SelectMember.route
        ) { backStackEntry ->
            val excludeString = backStackEntry.arguments?.getString("exclude") ?: ""
            val excludedList = if (excludeString.isNotBlank()) excludeString.split(",").map { it.trim() } else emptyList()

            SelectMemberScreen(
                excludedNames = excludedList,
                onNavigateBack = { navController.popBackStack() },
                onSelectionConfirmed = { selectedMembers -> // selectedMembers is ArrayList<Members>
                    // MAP Members -> DraftMember (Parcelable)
                    val draftMembers = selectedMembers.map {
                        DraftMember(it.id, it.name, it.contact, it.note)
                    }

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_members", ArrayList(draftMembers))

                    navController.popBackStack()
                }
            )
        }
        composable(NavItem.Analytics.route) {
            AnalyticsScreen()
        }
        composable(NavItem.Settings.route) {
            SettingsScreen(
                onNavigateToProfile = { navController.navigateSingleTopTo(NavItem.Profile.route) }
            )
        }
        composable(NavItem.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(NavItem.CompleteProfile.route) {
            CompleteProfileScreen(onComplete = {
                navController.navigateSingleTopTo(NavItem.Dashboard.route)
            })
        }
        composable(NavItem.GalleryTransaction.route) {
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
        composable(NavItem.ScanTransaction.route) {
            ScanTransactionScreen(
                onNavigateToManual = {
                    navController.navigate(NavItem.ManualTransaction.route)
                },
                // Updated to accept (Uri, String?)
                onImagePicked = { uri, ocrText ->
                    // 1. Encode the URI
                    val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())

                    // 2. Encode the OCR Text (handle nulls safely)
                    val encodedOcrText = URLEncoder.encode(ocrText ?: "", StandardCharsets.UTF_8.toString())

                    // 3. Construct the route with the query parameter
                    // We manually construct the string to match the Review route format
                    val route = "review_transaction/$encodedUri?ocrText=$encodedOcrText"

                    navController.navigate(route)
                }
            )
        }
        composable(NavItem.ManualTransaction.route) {
            ManualTransactionScreen(
                onNavigateBack = { navController.navigateSingleTopTo(NavItem.Dashboard.route) }
            )
        }
        composable(
            route = NavItem.ReviewTransaction.route, // This now includes "?ocrText={ocrText}"
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("ocrText") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString("imageUri")
            val ocrText = backStackEntry.arguments?.getString("ocrText")

            // Create ViewModel
            val viewModel: ReviewTransactionViewModel = viewModel(
                factory = AppViewModelProvider.Factory
            )

            // Initialize Data ONLY ONCE
            LaunchedEffect(imageUriString) {
                viewModel.setImageUri(imageUriString)

                // If OCR text was passed, try to extract Date and Amount
                if (!ocrText.isNullOrEmpty()) {
                    viewModel.parseReceiptText(ocrText)
                }
            }

            ReviewTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(NavItem.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavItem.SplitResult.route) {
            SplitResultScreen(
                navController = navController,
                onNavigateHome = {
                    navController.navigate(NavItem.Dashboard.route) {
                        popUpTo(NavItem.Dashboard.route) { inclusive = true }
                    }
                }
            )
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
    }
}