package com.menac1ngmonkeys.monkeyslimit.ui.profile

import AppViewModelProvider
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysDatePicker
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

    // Validation State (For Dialog)
    var validationError by remember { mutableStateOf<String?>(null) }

    // Field-Specific Error States
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }
    var incomeError by remember { mutableStateOf<String?>(null) }

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
                        viewModel.useDefaultProfilePhoto()
                    }
                )
            }
        }
    }

    // --- DATE PICKER & SAVE DIALOGS ---
    MonkeysDatePicker(
        show = showDatePicker,
        initialDate = if (uiState.birthDate != 0L) Date(uiState.birthDate) else Date(),
        disableFutureDates = true, // Restricts selection to Today or earlier
        onDismiss = { showDatePicker = false },
        onDateSelected = { selectedMillis ->
            if (selectedMillis != null) {
                viewModel.updateBirthDate(selectedMillis)
                birthDateError = null // Clear error when valid date picked
            }
            showDatePicker = false
        }
    )

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Save Changes",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to save these changes?",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        viewModel.saveProfile(context = context, onSuccess = { navController.popBackStack() })
                    },
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { validationError = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Input Required",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = validationError ?: "",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { validationError = null },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACF69)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
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
                EditField(
                    label = "Name",
                    value = uiState.name,
                    isError = nameError != null,
                    errorMessage = nameError,
                    onChange = { viewModel.updateName(it); nameError = null },
                    readOnly = false
                )

                EditField(
                    label = "Email Address",
                    value = uiState.email,
                    isError = emailError != null,
                    errorMessage = emailError,
                    onChange = { viewModel.updateEmail(it); emailError = null },
                    readOnly = true
                )

                EditField(
                    label = "Phone Number",
                    value = uiState.mobileNumber,
                    isError = mobileError != null,
                    errorMessage = mobileError,
                    onChange = {
                        viewModel.updatePhone(it.filter { c -> c.isDigit() || c == '+' }.take(15))
                        mobileError = null
                    },
                    readOnly = false
                )

                DropdownField(
                    label = "Gender",
                    selectedValue = uiState.gender,
                    options = genderOptions,
                    isError = genderError != null,
                    errorMessage = genderError
                ) { viewModel.updateGender(it); genderError = null }

                val dateString = if (uiState.birthDate == 0L) "" else {
                    SimpleDateFormat("dd / MM / yyyy", Locale.getDefault()).format(Date(uiState.birthDate))
                }
                DatePickerField(
                    label = "Birth Date",
                    value = dateString,
                    isError = birthDateError != null,
                    errorMessage = birthDateError
                ) { showDatePicker = true }

                val marriageOptions = listOf("Single", "Married")
                DropdownField(
                    label = "Marriage Status",
                    selectedValue = if (uiState.isMarried) "Married" else "Single",
                    options = marriageOptions
                ) { viewModel.updateMarriageStatus(it == "Married") }

                DropdownField(
                    label = "Job",
                    selectedValue = uiState.job,
                    options = jobOptions,
                    isError = jobError != null,
                    errorMessage = jobError
                ) { viewModel.updateJob(it); jobError = null }

                DropdownField(
                    label = "Income",
                    selectedValue = uiState.income,
                    options = incomeOptions,
                    isError = incomeError != null,
                    errorMessage = incomeError
                ) { viewModel.updateIncome(it); incomeError = null }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        var hasError = false
                        val indoPhoneRegex = "^(?:\\+62|62|0)8[0-9]{8,11}$".toRegex()

                        // FULL VALIDATION BLOCK (Checks all fields simultaneously)
                        if (uiState.name.isBlank()) {
                            nameError = "Required"
                            hasError = true
                        } else if (uiState.name.trim().length < 3) {
                            nameError = "Min 3 chars"
                            hasError = true
                        }

                        if (uiState.email.isBlank()) {
                            emailError = "Required"
                            hasError = true
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()) {
                            emailError = "Invalid email format"
                            hasError = true
                        }

                        if (uiState.mobileNumber.isBlank()) {
                            mobileError = "Required"
                            hasError = true
                        } else if (!indoPhoneRegex.matches(uiState.mobileNumber)) {
                            mobileError = "Invalid format (e.g., 08... or +628...)"
                            hasError = true
                        }

                        if (uiState.gender.isBlank()) { genderError = "Required"; hasError = true }
                        if (uiState.birthDate == 0L) { birthDateError = "Required"; hasError = true }
                        if (uiState.job.isBlank()) { jobError = "Required"; hasError = true }
                        if (uiState.income.isBlank()) { incomeError = "Required"; hasError = true }

                        if (hasError) {
                            validationError = "Please check the highlighted fields below."
                        } else {
                            showSaveDialog = true
                        }
                    },
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
private fun EditField(
    label: String,
    value: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    onChange: (String) -> Unit,
    readOnly: Boolean = false,
) {
    Column(Modifier.fillMaxWidth()) {
        Text("$label:", color = Color(0xFF7A9B00), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            isError = isError,
            modifier = Modifier.fillMaxWidth().height(FieldHeight),
            readOnly = readOnly,
            trailingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF7A9B00)) },
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )
        if (isError && errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    isError: Boolean = false,
    errorMessage: String? = null,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text("$label:", color = Color(0xFF7A9B00), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box {
            Card(
                modifier = Modifier.fillMaxWidth().height(FieldHeight).clickable { expanded = true },
                shape = FieldShape,
                border = if (isError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedValue.ifEmpty { "Select $label" }, modifier = Modifier.weight(1f), color = if (selectedValue.isEmpty()) Color.Gray else Color.Black)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF7A9B00))
                }
            }
            DropdownMenu(
                containerColor = MaterialTheme.colorScheme.surface,
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelected(it); expanded = false }) }
            }
        }
        if (isError && errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    onClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text("$label:", color = Color(0xFF7A9B00), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))

        // 1. Wrap in a Box to allow layering
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight), // Removed .clickable() from here
                trailingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF7A9B00)) },
                shape = FieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorContainerColor = Color(0xFFF5F5F5)
                ),
                singleLine = true
            )

            // 2. Add a transparent overlay that matches the size and shape
            // This sits ON TOP of the text field and captures the click.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(FieldShape)
                    .clickable { onClick() }
            )
        }

        if (isError && errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}