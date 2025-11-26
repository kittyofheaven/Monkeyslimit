package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppFAB(
    onClick: () -> Unit,
    size: Dp = 60.dp,
    offsetY: Dp = 45.dp,
    offsetX: Dp = 0.dp
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier
            .size(size)
            .offset(y = offsetY, x = offsetX)
//            .background(MaterialTheme.colorScheme.onBackground)
        ,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ), // Disable the shadow
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Icon(
            painter = painterResource(NavItem.Transaction.iconId),
            contentDescription = NavItem.Transaction.title,
            tint = Color.White
        )
    }
}