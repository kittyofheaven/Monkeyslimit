package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem

@Composable
fun TransactionDialog(
    onDismiss: () -> Unit,
    onItemClick: (DialogItem) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier,
            shape = RoundedCornerShape(50),
        ) {
            Row(
                modifier = Modifier
                    .padding(32.dp, 20.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DialogItemComposable(
                    dialogItem = DialogItem.AI,
                    onItemClick = onItemClick
                )
                DialogItemComposable(
                    dialogItem = DialogItem.Manual,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

// TransactionPopUp.kt
@Composable
fun ExpandableTransactionMenu(
    visible: Boolean,
    onItemClick: (NavItem) -> Unit
) {
    // We use Row to stack items horizontally
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(bottom = 12.dp) // Space between the menu and the main FAB
            .offset(y = 45.dp)
    ) {
        NavItem.FABMenu.forEach { item ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + expandVertically() + scaleIn(),
                exit = fadeOut() + shrinkVertically() + scaleOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Small FAB for each action
                    SmallFloatingActionButton(
                        onClick = { onItemClick(item) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 0.dp,
                            hoveredElevation = 0.dp
                        ),
                        modifier = Modifier.size(45.dp)
                    ) {
                        Icon(
                            painter = painterResource(item.iconId),
                            contentDescription = item.title,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionPopUpPreview() {
    TransactionDialog(onDismiss = {}, onItemClick = {})
}