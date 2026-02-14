package com.menac1ngmonkeys.monkeyslimit

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.menac1ngmonkeys.monkeyslimit.data.local.AppDatabase
import com.menac1ngmonkeys.monkeyslimit.data.local.seeders.BudgetsSeeder
import com.menac1ngmonkeys.monkeyslimit.data.local.seeders.SeedCoordinator
import com.menac1ngmonkeys.monkeyslimit.data.worker.NotificationHelper
import com.menac1ngmonkeys.monkeyslimit.ui.auth.AuthPrimaryGreen
import com.menac1ngmonkeys.monkeyslimit.ui.auth.CompleteProfileScreen
import com.menac1ngmonkeys.monkeyslimit.ui.auth.LoginScreen
import com.menac1ngmonkeys.monkeyslimit.ui.auth.SignUpScreen
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.AppFAB
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.BottomBar
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavGraph
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.TopBar
import com.menac1ngmonkeys.monkeyslimit.ui.splash.SplashScreenContent
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileAuthStatus
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.ExpandableTransactionMenu
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.TransactionDialog
import com.menac1ngmonkeys.monkeyslimit.utils.navigateSingleTopTo
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ProfileViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels { AppViewModelProvider.Factory }
    private val authViewModel: AuthViewModel by viewModels { AppViewModelProvider.Factory }
    private val profileViewModel: ProfileViewModel by viewModels { AppViewModelProvider.Factory }

    // ADD: Permission Launcher
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Notification", "User granted permission 🐒")
        } else {
            Log.w("Notification", "User denied permission. Reminder won't show.")
        }
    }

    private fun getGoogleSignInClient() = GoogleSignIn.getClient(
        this,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    ).apply {
        signOut()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize Channel and Request Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        var isReady = false

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value.isLoading || !isReady
        }

        enableEdgeToEdge()

//        lifecycleScope.launch(Dispatchers.IO) {
//            // hard reset the user table
//            val db = AppDatabase.getDatabase(this@MainActivity)
//            db.clearAllTables()
//        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@MainActivity)
            SeedCoordinator.seedDev(db)
        }

        // Start Debug DB
        // End Debug DB

        setContent {
            val context = LocalContext.current
            isReady = true

            val googleSignInLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account.idToken?.let { authViewModel.signInWithGoogle(it) }
                } catch (e: Exception) {
                    Toast.makeText(context, "Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }

            MonkeyslimitTheme {
                AuthGatekeeper(
                    authViewModel = authViewModel,
                    splashViewModel = splashViewModel,
                    profileViewModel = profileViewModel,
                    onGoogleSignIn = {
                        val client = getGoogleSignInClient()
                        client.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(client.signInIntent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AuthGatekeeper(
    authViewModel: AuthViewModel,
    splashViewModel: SplashViewModel,
    profileViewModel: ProfileViewModel,
    onGoogleSignIn: () -> Unit
) {
    val splashUiState by splashViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isLoginScreen by remember { mutableStateOf(true) }
    var showBranding by remember { mutableStateOf(true) }

    // 1. Splash Logic
    LaunchedEffect(splashUiState.isLoading) {
        if (!splashUiState.isLoading) {
            delay(1500)
            showBranding = false
        }
    }

    // 2. Sync Logic
    LaunchedEffect(authUiState.currentUser) {
        if (authUiState.currentUser != null) {
            authViewModel.startRealtimeSync()

            // --- NEW: Seed user-specific budgets upon successful login ---
            val db = AppDatabase.getDatabase(context)
            val uid = authUiState.currentUser!!.uid
            BudgetsSeeder.seedForUser(db.budgetsDao(), uid)
        }
    }

    // --- DECISION LOGIC ---
    if (showBranding) {
        SplashScreenContent()
        return
    }

    if (authUiState.currentUser == null) {
        // Not Logged In
        if (isLoginScreen) {
            LoginScreen(
                onGoogleSignIn = onGoogleSignIn,
                onEmailSignIn = { e, p -> authViewModel.signInWithEmail(e, p) },
                onNavigateToSignUp = { isLoginScreen = false }
            )
        } else {
            // FIX: Added income and isMarried to the callback parameters
            SignUpScreen(
                onNavigateToLogin = { isLoginScreen = true },
                onEmailSignUp = { email, pass, fName, lName, phone, job, bDay, gender, income, isMarried ->
                    authViewModel.signUpWithEmail(
                        email, pass, fName, lName, phone, job, bDay, gender, income, isMarried
                    )
                }
            )
        }
        return
    }

    Log.d("AuthGatekeeper", "Current Profile Status: ${profileUiState.status}")

    // User is Logged In -> Check Sealed Status
    when (profileUiState.status) {
        is ProfileAuthStatus.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuthPrimaryGreen)
            }
        }
        is ProfileAuthStatus.Ghost -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuthPrimaryGreen)
                Text("Syncing data...", Modifier.padding(top=64.dp))
            }
        }
        is ProfileAuthStatus.Incomplete -> {
            CompleteProfileScreen(onComplete = {
                Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
            })
        }
        is ProfileAuthStatus.Verified -> {
            MonkeysLimitApp()
        }
    }
}

@Composable
fun MonkeysLimitApp(
    profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    var showTransactionDialog by remember { mutableStateOf(false) }

    // Observe Profile State to get User Name
    val profileState by profileViewModel.uiState.collectAsState()
    val userName = profileState.name.ifEmpty { "User" }.substringBefore(" ")

    val allNavItems = remember {
        NavItem.mainNavItems + NavItem.subNavItems
    }
    val currentScreen = allNavItems.find { it.route == currentRoute }
    val showTopBar = currentScreen?.showTopBar
    val showBottomBar = currentScreen?.showBottomBar

    val topBarTitle = if (currentScreen == NavItem.Dashboard) {
        "Hi, $userName"
    } else {
        currentScreen?.title ?: "Hi, $userName..."
    }

    if (showTransactionDialog) {
        TransactionDialog(
            onDismiss = { showTransactionDialog = false },
            onItemClick = { item ->
                showTransactionDialog = false
                navController.navigate(item.route)
            }
        )
    }

    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
            ,
            topBar = {
                if (showTopBar == true) {
                    TopBar(
                        title = topBarTitle,
                        currentRoute = currentRoute,
                        profileImageUrl = profileState.photoUrl,
                        onProfileClick = { navController.navigateSingleTopTo(NavItem.Profile.route) },
                        onSettingsClick = { navController.navigateSingleTopTo(NavItem.Settings.route) },
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            },
            floatingActionButton = {
//            if (showNavElements && showBottomBar) {
//                AppFAB(onClick = { showTransactionDialog = true })
//            }
                if (showBottomBar == true) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // The Capsule Menu
                        ExpandableTransactionMenu(
                            visible = isMenuExpanded,
                            onItemClick = { item ->
                                isMenuExpanded = false
                                navController.navigate(item.route)
                            }
                        )

                        // The Main FAB
                        AppFAB(
                            onClick = { isMenuExpanded = !isMenuExpanded },
                            // Toggle between Plus and Close icon
                            iconId = if (isMenuExpanded) R.drawable.close_40dp else NavItem.Transaction.iconId,
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            bottomBar = {
                if (showBottomBar == true) {
                    BottomBar(
                        navController = navController,
                        navItems = NavItem.mainNavItems,
                        currentRoute = currentRoute,
                        cutoutBackgroundColor = if (currentRoute == NavItem.Dashboard.route) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )

            // 1. DISMISSAL LAYER
            // This invisible box catches all clicks outside the FAB menu
            if (isMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No ripple when clicking outside
                        ) {
                            isMenuExpanded = false
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MonkeysLimitAppPreview() {
    MonkeysLimitApp()
}

// Debug DB
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
//            // Test Categories // INI DIHILANGKAN KARENA UDAH ADA SEEDERS
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
// akhir test DB