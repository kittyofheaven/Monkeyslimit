package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    currentRoute: String?,
    profileImageUrl: String?,
    onProfileClick: () -> Unit,
    onSettingsClick: ()-> Unit,
    onNavigateUp: () -> Unit,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // APP LOGO
                if (currentRoute == NavItem.Dashboard.route) {
                    Image(
                        painter = painterResource(id = com.menac1ngmonkeys.monkeyslimit.R.drawable.logo_monkeys_limit), // Replace with your actual drawable ID
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(60.dp) // Adjust size as needed
                            .padding(end = 12.dp) // Space between logo and text
                    )
                }

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
            }
        },
        actions = {
            if (currentRoute == NavItem.Dashboard.route) {
                IconButton(onClick = onProfileClick) {
                    // 2. Logic to show Profile Picture or Fallback Icon
                    if (!profileImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape), // Makes the image circular
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(NavItem.Profile.iconId),
                            error = painterResource(NavItem.Profile.iconId)
                        )
                    } else {
                        Icon(
                            painter = painterResource(NavItem.Profile.iconId),
                            contentDescription = NavItem.Profile.title
                        )
                    }
                }
            }
        },
        navigationIcon = {
            val showBackArrow = currentRoute in NavItem.subNavItems.map { it.route } ||
                    currentRoute == NavItem.SmartSplit.route

            if (showBackArrow) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
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
        in 0..11 -> "Good Morning!"
        in 12..15 -> "Good Afternoon!"
        in 16..18 -> "Good Evening!"
        else -> "Good Night!"
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    TopBar(
        title = "Hi, Welcome Back",
        currentRoute = NavItem.Dashboard.route,
        onProfileClick = {},
        onSettingsClick = {},
        onNavigateUp = {},
        profileImageUrl = null
    )
}