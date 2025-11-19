package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.utils.navigateToTopLevel

const val CUTOUT_SIZE = 90f

@Composable
fun BottomBar(
    navController: NavHostController,
    navItems: List<NavItem>,
    currentRoute: String?
) {
    val cutoutShape = BottomBarCutoutShape(fabDiameter = CUTOUT_SIZE.dp)

    Surface(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp)),
        shape = cutoutShape,
        tonalElevation = NavigationBarDefaults.Elevation,
        shadowElevation = NavigationBarDefaults.Elevation
    ) {
        Row(
            modifier = Modifier
                // NavBar BG Color
                // 0xFF232121 (Dark Mode)
                // 0xFF052224 (Light Mode)
                .background(Color(0xFF232121))
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                if (item != NavItem.Transaction) {
                    val selected = currentRoute == item.route

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (selected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) // NavItem Selected BG Color
                                else
                                    Color.Transparent
                            )
                            .size(50.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (!selected) navController.navigateToTopLevel(item.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(item.iconId),
                            contentDescription = item.title,
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary // NavItem Selected Icon Color
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant // NavItem Icon Color
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(CUTOUT_SIZE.dp)) // FAB space
                }
            }
        }
    }
}