package com.menac1ngmonkeys.monkeyslimit.ui.transaction

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ScanTransactionViewModel
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanTransactionScreen(
    onNavigateToManual: () -> Unit,
    onImagePicked: (Uri) -> Unit = {},
    viewModel: ScanTransactionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("ScanScreen", "Image picked from gallery: $uri")
            onImagePicked(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        if (permissionState.status.isGranted) {
            ScanContent(
                isFlashOn = uiState.isFlashOn,
                onToggleFlash = { viewModel.toggleFlash() },
                onOpenGallery = { galleryLauncher.launch("image/*") },
                onPhotoCaptured = { uri -> onImagePicked(uri) }
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required to scan receipts.", color = Color.White)
            }
        }
    }
}

@Composable
private fun ScanContent(
    isFlashOn: Boolean,
    onToggleFlash: () -> Unit,
    onOpenGallery: () -> Unit,
    onPhotoCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose { cameraController.unbind() }
    }

    LaunchedEffect(isFlashOn) {
        cameraController.enableTorch(isFlashOn)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = onToggleFlash) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color(0xFFFDD86A),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .border(4.dp, Color(0xFFFDD86A), CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        takePhotoToCache(context, cameraController, onPhotoCaptured)
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 40.dp)
            ) {
                YellowCircleButton(
                    iconId = NavItem.GalleryTransaction.iconId,
                    onClick = onOpenGallery
                )
            }
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
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(0xFFFDD86A))
            .clickable(onClick = onClick),
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

/**
 * Captures photo to the app's CACHE directory using [FileProvider].
 * This ensures the photo is temporary and doesn't clutter the user's Gallery.
 */
private fun takePhotoToCache(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Uri) -> Unit
) {
    // 1. Create a temporary file in the app's CACHE directory
    val photoFile = File.createTempFile(
        "temp_scan_",
        ".jpg",
        context.cacheDir
    )

    // 2. Create OutputOptions pointing to this file
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // 3. Take Picture
    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("ScanScreen", "Photo capture failed: ${exc.message}", exc)
                Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // 4. Get the URI using FileProvider
                val savedUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider", // Authority from AndroidManifest
                    photoFile
                )
                onPhotoCaptured(savedUri)
            }
        }
    )
}