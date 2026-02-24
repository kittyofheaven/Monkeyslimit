package com.menac1ngmonkeys.monkeyslimit.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import com.smarttoolfactory.cropper.settings.CropType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImagePreviewScreen(
    navController: NavController,
    imageUri: Uri,
    aspectRatioValue: Float = 1f,
    isDynamicCrop: Boolean = false,
    bottomBarText: String? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isCropping by remember { mutableStateOf(false) }

    // Load bitmap with EXIF orientation correction
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val loaded = loadBitmapFromUri(context, imageUri)
            withContext(Dispatchers.Main) { imageBitmap = loaded }
        }
    }

    val handleSizePx = with(density) { 20.dp.toPx() }

    val targetCropType = if (isDynamicCrop) CropType.Dynamic else CropType.Static
    val targetContentScale = if (isDynamicCrop) ContentScale.Fit else ContentScale.FillWidth

    // UPDATED: Properties to force full width and bound enforcement
    val cropProperties = CropDefaults.properties(
        cropOutlineProperty = CropOutlineProperty(
            OutlineType.Rect,
            RectCropShape(id = 0, title = "Square")
        ),
        cropType = targetCropType,
        handleSize = handleSizePx,
        contentScale = targetContentScale, // Forces the image to the full width
        aspectRatio = AspectRatio(aspectRatioValue),
        fling = false, // Enabling fling helps the library calculate momentum-based bounds
        maxZoom = 10f,
        zoomable = true,
        pannable = true,
        rotatable = false
    )

    val cropStyle = CropDefaults.style(
        overlayColor = Color.Black.copy(alpha = 0.6f),
        drawOverlay = true,
        drawGrid = true
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (imageBitmap != null) {
            ImageCropper(
                modifier = Modifier.fillMaxSize(),
                imageBitmap = imageBitmap!!,
                contentDescription = "Image Cropper",
                cropProperties = cropProperties,
                cropStyle = cropStyle,
                crop = isCropping,
                onCropStart = { },
                onCropSuccess = { croppedBitmap: ImageBitmap ->
                    scope.launch {
                        val androidBitmap = croppedBitmap.asAndroidBitmap()
                        val savedUri = saveBitmapToCache(context, androidBitmap)
                        if (savedUri != null) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle?.set("edited_image_uri", savedUri)
                            navController.popBackStack()
                        }
                    }
                }
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        /* --- UI Overlays (Top Bar) --- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Text("Edit Photo", color = Color.White, style = MaterialTheme.typography.titleMedium)
            IconButton(
                onClick = { isCropping = true },
                enabled = !isCropping && imageBitmap != null
            ) {
                if (isCropping) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Done, contentDescription = "Save", tint = Color.White)
                }
            }
        }

        /* --- UI Overlays (Bottom Bar) --- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Text(
                text = bottomBarText ?: "Pinch to Zoom",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = {
                    // Rotate the actual bitmap data so the cropper recalculates bounds
                    imageBitmap?.let { currentBitmap ->
                        val rotated = rotateBitmap(currentBitmap.asAndroidBitmap(), 90f)
                        imageBitmap = rotated.asImageBitmap()
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White)
            }
        }
    }
}

// --- Load bitmap with EXIF orientation correction ---
private fun loadBitmapFromUri(context: Context, uri: Uri): ImageBitmap? {
    return try {
        // 1. Decode the raw bitmap
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        inputStream?.close()

        // 2. Read EXIF orientation tag
        val exifStream = context.contentResolver.openInputStream(uri) ?: return bitmap.asImageBitmap()
        val exif = ExifInterface(exifStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        exifStream.close()

        // 3. Apply the correct rotation based on EXIF data
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotationDegrees != 0f) {
            rotateBitmap(bitmap, rotationDegrees).asImageBitmap()
        } else {
            bitmap.asImageBitmap()
        }
    } catch (e: Exception) { null }
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val fileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        Uri.fromFile(file)
    } catch (e: Exception) { null }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}