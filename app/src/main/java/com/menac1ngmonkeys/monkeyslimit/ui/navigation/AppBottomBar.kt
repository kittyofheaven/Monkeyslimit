package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.utils.navigateToTopLevel

const val CUTOUT_SIZE = 60f

@Composable
fun BottomBar(
    navController: NavHostController,
    navItems: List<NavItem>,
    currentRoute: String?
) {
    val cutoutShape = bottomBarCutoutShape(fabDiameter = CUTOUT_SIZE.dp)

    Surface(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
//            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
            .height(70.dp)
        ,
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
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                if (item != NavItem.Transaction) {
                    val selected = currentRoute == item.route

                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!selected) navController.navigateToTopLevel(item.route)
                            }
                            .width(45.dp)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            modifier = Modifier
//                                .clip(RoundedCornerShape(10.dp))
//                                .background(
//                                    if (selected)
//                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) // NavItem Selected BG Color
//                                    else
//                                        Color.Transparent
//                                )
                                .size(35.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(30.dp),
                                painter = painterResource(item.iconId),
                                contentDescription = item.title,
                                tint = if (selected)
                                    MaterialTheme.colorScheme.primary // NavItem Selected Icon Color
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant // NavItem Icon Color
                            )
                        }

                        Text(
                            text =
                                if (item.title == "Dashboard") {
                                    "Home"
                                } else {
                                    item.title
                                }
                            ,
                            fontSize = TextUnit(8f, TextUnitType.Sp),
                            color =
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                        )

                    }
                } else {
                    Spacer(modifier = Modifier.width((CUTOUT_SIZE * 0.6f).dp)) // FAB space
                }
            }
        }
    }
}