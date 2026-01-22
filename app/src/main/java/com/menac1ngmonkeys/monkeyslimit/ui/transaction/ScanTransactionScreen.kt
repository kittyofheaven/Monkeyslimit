package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanTransactionScreen(
    onNavigateToManual: () -> Unit,
    // This callback is now triggered by BOTH Gallery picks AND Camera captures
    onImagePicked: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // --- 1. Gallery Launcher Setup ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("ScanScreen", "Image picked from gallery: $uri")
            // Pass the URI to the parent (NavGraph) to navigate to ReviewScreen
            onImagePicked(uri)
        }
    }

    // --- 2. Request Camera Permission on Start ---
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
                onNavigateToManual = onNavigateToManual,
                // Launch Gallery
                onOpenGallery = { galleryLauncher.launch("image/*") },
                // Handle Camera Capture
                onPhotoCaptured = { uri ->
                    Log.d("ScanScreen", "Photo captured: $uri")
                    onImagePicked(uri)
                }
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
    onNavigateToManual: () -> Unit,
    onOpenGallery: () -> Unit,
    onPhotoCaptured: (Uri) -> Unit // New callback for successful capture
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Setup Camera Controller
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    var isFlashOn by remember { mutableStateOf(false) }

    // Bind Camera to Lifecycle
    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose { cameraController.unbind() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // --- 1. Camera Preview View ---
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

        // --- 2. Flash Button (Top Right) ---
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

        // --- 3. Scanning Brackets Overlay (Center) ---
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

                // Top Left
                drawArc(color, 180f, 90f, false, Offset(0f, 0f), Size(cornerLength * 2, cornerLength * 2), style = style)
                // Top Right
                drawArc(color, 270f, 90f, false, Offset(size.width - cornerLength * 2, 0f), Size(cornerLength * 2, cornerLength * 2), style = style)
                // Bottom Left
                drawArc(color, 90f, 90f, false, Offset(0f, size.height - cornerLength * 2), Size(cornerLength * 2, cornerLength * 2), style = style)
                // Bottom Right
                drawArc(color, 0f, 90f, false, Offset(size.width - cornerLength * 2, size.height - cornerLength * 2), Size(cornerLength * 2, cornerLength * 2), style = style)
            }
        }

        // --- 4. Bottom Controls ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp, start = 20.dp, end = 20.dp)
        ) {
            // Shutter Button (Center)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .border(4.dp, Color(0xFFFDD86A), CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        // Trigger Camera Capture
                        takePhoto(context, cameraController, onPhotoCaptured)
                    }
            )

            // Gallery Button (Right)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 40.dp)
            ) {
                YellowCircleButton(
                    iconId = NavItem.GalleryTransaction.iconId,
                    onClick = onOpenGallery // Triggers the launcher
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

private fun takePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Uri) -> Unit // Success Callback
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = android.content.ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MonkeysLimit")
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        .build()

    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("ScanScreen", "Photo capture failed: ${exc.message}", exc)
                Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // Get the URI of the saved image
                val savedUri = output.savedUri
                if (savedUri != null) {
                    onPhotoCaptured(savedUri)
                } else {
                    Toast.makeText(context, "Photo saved but URI is null.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}