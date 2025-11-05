package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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

@Preview(showBackground = true)
@Composable
private fun TransactionPopUpPreview() {
    TransactionDialog(onDismiss = {}, onItemClick = {})
}