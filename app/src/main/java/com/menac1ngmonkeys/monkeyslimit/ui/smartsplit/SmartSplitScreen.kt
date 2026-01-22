package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import android.Manifest
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmartSplitScreen(
    onGalleryClick: () -> Unit = {},
    onManualEntryClick: () -> Unit = {}
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black, // Set background to black to match camera theme
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        if (permissionState.status.isGranted) {
            CameraContent(
                onGalleryClick = onGalleryClick,
                onManualEntryClick = onManualEntryClick
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera permission required", color = Color.White)
            }
        }
    }
}

@Composable
fun CameraContent(
    onGalleryClick: () -> Unit,
    onManualEntryClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Initialize Controller
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }

    // 2. DisposableEffect for lifecycle management
    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            cameraController.unbind()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        // 3. Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- FLASH BUTTON (Top Right) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = {
                isFlashOn = !isFlashOn
                cameraController.enableTorch(isFlashOn)
            }) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color(0xFFFDD86A),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // --- BRACKETS (Center Overlay) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 150.dp, start = 40.dp, end = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val cornerLength = 40.dp.toPx()
                val color = Color.White.copy(alpha = 0.8f)
                val style = Stroke(width = strokeWidth)

                drawArc(color, 180f, 90f, false, Offset(0f, 0f), Size(cornerLength * 2, cornerLength * 2), style = style)
                drawArc(color, 270f, 90f, false, Offset(size.width - cornerLength * 2, 0f), Size(cornerLength * 2, cornerLength * 2), style = style)
                drawArc(color, 90f, 90f, false, Offset(0f, size.height - cornerLength * 2), Size(cornerLength * 2, cornerLength * 2), style = style)
                drawArc(color, 0f, 90f, false, Offset(size.width - cornerLength * 2, size.height - cornerLength * 2), Size(cornerLength * 2, cornerLength * 2), style = style)
            }
        }

        // --- BOTTOM CONTROLS ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly // Distribute buttons evenly
        ) {
            // 1. Manual Entry
            YellowCircleButton(
                iconId = NavItem.ManualSplit.iconId,
                onClick = onManualEntryClick
            )

            // 2. Shutter Button (Center)
            Box(
                modifier = Modifier
                    .size(80.dp) // Bigger than side buttons
                    .border(4.dp, Color(0xFFFDD86A), CircleShape) // Yellow ring
                    .padding(6.dp) // Gap between ring and button
                    .clip(CircleShape)
                    .background(Color.White) // White button
                    .clickable {
                        takePhotoWithController(context, cameraController)
                    }
            )

            // 3. Gallery
            YellowCircleButton(
                iconId = NavItem.GallerySplit.iconId,
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
fun YellowCircleButton(
    iconId: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp) // Slightly smaller side buttons
            .clip(CircleShape)
            .background(Color(0xFFFDD86A))
            .clickable(onClick = onClick), // Standard clickable is fine here
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

private fun takePhotoWithController(
    context: Context,
    cameraController: LifecycleCameraController
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = android.content.ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MonkeysLimit")
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
        .build()

    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("ScanScreen", "Photo capture failed: ${exc.message}", exc)
                Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${output.savedUri}"
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                Log.d("ScanScreen", msg)
            }
        }
    )
}