package com.menac1ngmonkeys.monkeyslimit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.BottomBar
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavGraph
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme

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

@OptIn(ExperimentalMaterial3Api::class) // Topbar still experimental??
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(topBarTitle) },
                actions = {
                    if (currentRoute != NavItem.Settings.route) {
                        IconButton(onClick = {
                            navController.navigateToTopLevel(NavItem.Settings.route)
                        }) {
                            Icon(
                                painter = painterResource(NavItem.Settings.iconId),
                                contentDescription = NavItem.Settings.title
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigateToTopLevel(NavItem.Transaction.route)
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = 50.dp)
                    .border(
                        width = 10.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ) // Disable the shadow
            ) {
                Icon(
                    painter = painterResource(NavItem.Transaction.iconId),
                    contentDescription = NavItem.Transaction.title
                )
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        bottomBar = {
            BottomBar(navController, navItems, currentRoute)
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