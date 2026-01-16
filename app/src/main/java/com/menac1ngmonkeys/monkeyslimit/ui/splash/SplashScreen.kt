package com.menac1ngmonkeys.monkeyslimit.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.menac1ngmonkeys.monkeyslimit.R

@Composable
fun SplashScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Match your theme background
//            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_monkeys_limit),
                contentDescription = "Logo",
                modifier = Modifier.size(240.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "MONKEYS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF6C8B08),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "LIMIT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF9E3B6),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Text(
                text = "Good Finance, Better Life",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent()
}