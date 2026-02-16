package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.auth.AuthPrimaryGreen
import com.menac1ngmonkeys.monkeyslimit.ui.auth.AuthPrimaryYellow

@Composable
fun MonkeyLoadingScreen(
    loadingText: String = "Analyzing...",
    monkeyImageRes: Int = R.drawable.logo_monkeys_limit,
    isFinished: Boolean = false, // NEW: Tells the animation to hit 100%
    onDismiss: () -> Unit = {}   // NEW: Tells the parent to hide this screen
) {
    // --- 1. Animation States ---
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing_monkey")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset"
    )

    val progress = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    // --- 2. Smart Progress Logic ---
    LaunchedEffect(isFinished) {
        if (!isFinished) {
            // Fake loading: slowly crawl to 90% over 15 seconds
            progress.animateTo(
                targetValue = 0.9f,
                animationSpec = tween(durationMillis = 15000, easing = FastOutSlowInEasing)
            )
        } else {
            // Done! Snap to 100% quickly
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            )
            // Fade out the entire overlay
            screenAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400)
            )
            // Tell the parent to actually remove the composable
            onDismiss()
        }
    }

    // --- 3. UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = screenAlpha.value } // Applies the fade out
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = monkeyImageRes),
                    contentDescription = "Loading...",
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { translationY = offsetY }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Updated to use the 0f to 1f progress value
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = AuthPrimaryGreen,
                    trackColor = AuthPrimaryYellow
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = loadingText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }
    }
}