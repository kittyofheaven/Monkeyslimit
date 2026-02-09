package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import AppViewModelProvider
import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SmartSplitViewModel
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmartSplitScreen(
    onImagePicked: (Uri) -> Unit = {},
    onHistoryClick: () -> Unit = {},
    viewModel: SmartSplitViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    // Wrapper to handle validation before navigation
    val handleImageSelection: (Uri) -> Unit = { uri ->
        viewModel.validateReceipt(context, uri) {
            // Only navigate if valid
            onImagePicked(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("SmartSplit", "Image picked from gallery: $uri")
            handleImageSelection(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    // --- VALIDATION ERROR DIALOG ---
    if (uiState.validationError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Invalid Receipt") },
            text = { Text(uiState.validationError!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) { Text("OK") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- LOADING OVERLAY ---
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            if (permissionState.status.isGranted) {
                CameraContent(
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onPhotoCaptured = { uri -> handleImageSelection(uri) },
                    onHistoryClick = onHistoryClick
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

        // Loading Indicator Overlay
        if (uiState.isValidating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) {}, // Block touches
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFDD86A))
                    Spacer(Modifier.height(16.dp))
                    Text("Analyzing Receipt...", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CameraContent(
    onGalleryClick: () -> Unit,
    onPhotoCaptured: (Uri) -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            cameraController.unbind()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- FLASH BUTTON ---
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            modifier = Modifier.fillMaxSize().padding(top = 100.dp, bottom = 150.dp, start = 40.dp, end = 40.dp),
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
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 50.dp, start = 20.dp, end = 20.dp)
        ) {
            // 1. History Button (Left)
            Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 40.dp)) {
                YellowCircleButton(imageVector = Icons.Default.History, onClick = onHistoryClick)
            }
            // 2. Shutter Button (Center)
            Box(modifier = Modifier.size(80.dp).align(Alignment.Center).border(4.dp, Color(0xFFFDD86A), CircleShape).padding(6.dp).clip(CircleShape).background(Color.White).clickable { takePhotoToCache(context, cameraController, onPhotoCaptured) })
            // 3. Gallery Button (Right)
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)) {
                YellowCircleButton(iconId = NavItem.GallerySplit.iconId, onClick = onGalleryClick)
            }
        }
    }
}

@Composable
fun YellowCircleButton(iconId: Int, onClick: () -> Unit) {
    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFFDD86A)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(painter = painterResource(iconId), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun YellowCircleButton(imageVector: ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFFDD86A)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(imageVector = imageVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
}

private fun takePhotoToCache(context: Context, cameraController: LifecycleCameraController, onPhotoCaptured: (Uri) -> Unit) {
    val photoFile = File.createTempFile("temp_receipt_", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    cameraController.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onError(exc: ImageCaptureException) {
            Log.e("SmartSplit", "Photo capture failed: ${exc.message}", exc)
            Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
        }
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            val savedUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            onPhotoCaptured(savedUri)
        }
    })
}