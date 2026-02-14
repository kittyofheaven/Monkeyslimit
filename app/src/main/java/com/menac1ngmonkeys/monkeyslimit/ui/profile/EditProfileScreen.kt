package com.menac1ngmonkeys.monkeyslimit.ui.profile

import AppViewModelProvider
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.viewmodel.EditProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val FieldHeight = 56.dp
private val FieldShape = RoundedCornerShape(50.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: EditProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val genderOptions = listOf("Male", "Female")
    val jobOptions = listOf("Student", "Employee", "Part-time Employee", "Entrepreneur", "Freelancer", "Housewife/Househusband")
    val incomeOptions = listOf("< Rp 1.000.000", "Rp 1.000.000 - Rp 3.000.000", "Rp 3.000.000 - Rp 5.000.000", "Rp 5.000.000 - Rp 8.000.000", "Rp 8.000.000 - Rp 12.000.000", "Rp 12.000.000 - Rp 20.000.000", "> Rp 20.000.000")

    var showDatePicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    // --- PHOTO PICKERS ---

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val encodedUri = Uri.encode(it.toString())
            navController.navigate(NavItem.ImagePreview.createRoute(encodedUri))
        }
    }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let {
                val encodedUri = Uri.encode(it.toString())
                navController.navigate(NavItem.ImagePreview.createRoute(encodedUri))
            }
        }
    }

    // --- CROPPER RESULT LISTENER ---
    val currentBackStackEntry = navController.currentBackStackEntry
    val editedImageUriState = currentBackStackEntry?.savedStateHandle
        ?.getLiveData<Uri>("edited_image_uri")
        ?.observeAsState()

    LaunchedEffect(editedImageUriState?.value) {
        editedImageUriState?.value?.let { uri ->
            viewModel.updateImageUri(uri)
            currentBackStackEntry?.savedStateHandle?.remove<Uri>("edited_image_uri")
        }
    }

    // --- PHOTO OPTIONS SHEET ---
    if (showPhotoOptions) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoOptions = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 16.dp)) {
                ListItem(
                    headlineContent = { Text("Take a Photo") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                    modifier = Modifier.clickable {
                        showPhotoOptions = false
                        val uri = createTempImageUri(context)
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                )
                ListItem(
                    headlineContent = { Text("Choose from Gallery") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable {
                        showPhotoOptions = false
                        galleryLauncher.launch("image/*")
                    }
                )
                // Option 3: Default Photo (Google)
                ListItem(
                    headlineContent = { Text("Use Default Photo") },
                    leadingContent = { Icon(Icons.Default.Refresh, null) },
                    modifier = Modifier.clickable {
                        showPhotoOptions = false
                        viewModel.useDefaultProfilePhoto() //
                    }
                )
            }
        }
    }

    // --- DATE PICKER & SAVE DIALOGS ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (uiState.birthDate != 0L) uiState.birthDate else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateBirthDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Changes") },
            text = { Text("Are you sure you want to save these changes?") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    viewModel.saveProfile(context = context, onSuccess = { navController.popBackStack() })
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    /* ================= UI CONTENT ================= */
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        /* ================= HEADER ================= */
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFFACF69))) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF7A9B00))
            }

            Text(
                text = "Edit Profile",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A9B00),
                style = MaterialTheme.typography.titleLarge
            )

            /* ===== Avatar ===== */
            Box(modifier = Modifier.size(140.dp).align(Alignment.Center), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    val imageModel = uiState.newImageUri ?: uiState.photoUrl
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF7A9B00), modifier = Modifier.size(60.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .offset((-12).dp, (-12).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8F9B20))
                        .clickable { showPhotoOptions = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        /* ================= FORM ================= */
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFACF69))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                EditField("Name", uiState.name) { viewModel.updateName(it) }
                EditField("Email Address", uiState.email) { viewModel.updateEmail(it) }
                EditField("Phone Number", uiState.mobileNumber) {
                    viewModel.updatePhone(it.filter { c -> c.isDigit() }.take(13))
                }
                DropdownField("Gender", uiState.gender, genderOptions) { viewModel.updateGender(it) }

                val dateString = if (uiState.birthDate == 0L) "" else {
                    SimpleDateFormat("dd / MM / yyyy", Locale.getDefault()).format(Date(uiState.birthDate))
                }
                DatePickerField("Birth Of Date", dateString) { showDatePicker = true }

                val marriageOptions = listOf("Single", "Married")
                DropdownField(
                    label = "Marriage Status",
                    selectedValue = if (uiState.isMarried) "Married" else "Single",
                    options = marriageOptions
                ) { viewModel.updateMarriageStatus(it == "Married") }

                DropdownField("Job", uiState.job, jobOptions) { viewModel.updateJob(it) }
                DropdownField("Income", uiState.income, incomeOptions) { viewModel.updateIncome(it) }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = FieldShape,
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACF69))
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save", color = Color.Black)
                    }
                }
            }
        }
    }
}

// --- Camera URI Helper ---
private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("camera_raw_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // Matches your manifest 'authorities'
        tempFile
    )
}

// --- Helpers ---
@Composable
private fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("$label:", color = Color(0xFF7A9B00), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth().height(FieldHeight),
            trailingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF7A9B00)) },
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DropdownField(label: String, selectedValue: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text("$label:", color = Color(0xFF7A9B00), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box {
            Card(
                modifier = Modifier.fillMaxWidth().height(FieldHeight).clickable { expanded = true },
                shape = FieldShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedValue.ifEmpty { "Select $label" }, modifier = Modifier.weight(1f), color = if (selectedValue.isEmpty()) Color.Gray else Color.Black)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF7A9B00))
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelected(it); expanded = false }) }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DatePickerField(label: String, value: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("$label:", color = Color(0xFF7A9B00), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth().height(FieldHeight).clickable { onClick() },
            trailingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF7A9B00)) },
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Transparent,
                disabledContainerColor = Color(0xFFF5F5F5),
                disabledTextColor = Color.Black
            ),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
    }
}