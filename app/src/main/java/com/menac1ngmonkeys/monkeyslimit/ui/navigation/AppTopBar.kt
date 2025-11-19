package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    currentRoute: String?,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            // This sets the background color of the TopAppBar
            containerColor = MaterialTheme.colorScheme.background,

            // This sets the color of the text title and any icons
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Column{
                Text(
                    text = title,
                    lineHeight = 0.5f.sp
                )
                if (currentRoute == NavItem.Dashboard.route) {
                    Text(
                        text = getGreetingMessage(),
                        fontSize = TextUnit(12f, TextUnitType.Sp),
                        fontWeight = FontWeight.Normal,
                        lineHeight = 0.5f.sp
                    )
                }
            }
        },
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

fun getCurrentHour(): Int {
    return LocalDateTime.now().hour
}

fun getGreetingMessage(): String {
    val currentHour = getCurrentHour()
    return when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..18 -> "Good Evening"
        else -> "Good Night"
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(
        title = "Hi, Welcome Back",
        currentRoute = NavItem.Dashboard.route,
        onSettingsClick = {}
    )
}