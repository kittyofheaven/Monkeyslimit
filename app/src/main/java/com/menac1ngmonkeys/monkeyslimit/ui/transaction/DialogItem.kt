package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R

sealed class DialogItem (
    val name: String,
    val route: String,
    val iconId: Int,
) {
    // Sementara yg Gallery dan Camera di hold dulu karena mungkin dipake di tempat lain
    object Gallery : DialogItem (
        name = "Gallery",
        route = "gallery",
        iconId = R.drawable.image_40px
    )

    object Camera : DialogItem (
        name = "Camera",
        route = "camera",
        iconId = R.drawable.camera_40px
    )

    object Manual : DialogItem (
        name = "Manual",
        route = "manual",
        iconId = R.drawable.add_circle_40px
    )


    // Ini ga dipake harusnya
    object AI : DialogItem (
        name = "AI",
        route = "ai",
        iconId = R.drawable.lightbulb_40px
    )

    companion object {
        val FABMenu = listOf(
//            AI,
//            Manual,
            Gallery,
            Camera,
        )
    }
}

@Composable
fun DialogItemComposable(
    dialogItem: DialogItem,
    onItemClick: (DialogItem) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onItemClick(dialogItem) }
    ) {
        Icon(
            painter = painterResource(dialogItem.iconId),
            contentDescription = null,
        )
        Spacer(Modifier.height(4.dp))
        Text(dialogItem.name)
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogItemPreview() {
    DialogItemComposable(DialogItem.Gallery, onItemClick = {})
}