package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SmartSplitScreen() {
    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("This is a SmartSplit page");
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SmartSplitScreenPreview() {  SmartSplitScreen()
}