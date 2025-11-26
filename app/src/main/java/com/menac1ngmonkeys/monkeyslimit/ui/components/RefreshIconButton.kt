package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import kotlinx.coroutines.launch

@Composable
fun RefreshIconButton(
    onClick: () -> Unit,
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            onClick()

            scope.launch {
                // Restart animation from 0 to 360
                rotation.stop()
                rotation.snapTo(0f)
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearEasing
                    )
                )
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            modifier = Modifier.rotate(rotation.value)
        )
    }
}
