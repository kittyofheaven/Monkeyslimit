package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    currentRoute: String?,
    onSettingsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        actions = {
            if (currentRoute != NavItem.Settings.route) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(NavItem.Settings.iconId),
                        contentDescription = NavItem.Settings.title
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(
        title = "Hi, Welcome",
        currentRoute = NavItem.Dashboard.route,
        onSettingsClick = {}
    )
}