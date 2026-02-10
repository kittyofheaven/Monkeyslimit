package com.menac1ngmonkeys.monkeyslimit.ui.profile

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri



private val FieldHeight = 56.dp
private val FieldShape = RoundedCornerShape(50.dp)

@Composable
fun EditProfileScreen(
    navController: NavHostController
) {

    val genderOptions = listOf(
        "Male",
        "Female"
    )

    val jobOptions = listOf(
        "Student",
        "Employee",
        "Part-time Employee",
        "Entrepreneur",
        "Freelancer",
        "Housewife/Househusband"
    )

    val incomeOptions = listOf(
        "< Rp 1.000.000",
        "Rp 1.000.000 - Rp 3.000.000",
        "Rp 3.000.000 - Rp 5.000.000",
        "Rp 5.000.000 - Rp 8.000.000",
        "Rp 8.000.000 - Rp 12.000.000",
        "Rp 12.000.000 - Rp 20.000.000",
        "> Rp 20.000.000"
    )

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
        }


    /* ================= DATE PICKER ================= */

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveDialog = false
            },

            title = {
                Text("Save Changes")
            },

            text = {
                Text("Are you sure you want to save these changes?")
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false

                        // TODO: nanti taruh logic save ke ViewModel di sini

                        navController.popBackStack()
                    }
                ) {
                    Text("Yes")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },

            confirmButton = {

                TextButton(onClick = {

                    datePickerState.selectedDateMillis?.let {

                        val formatter = SimpleDateFormat(
                            "dd / MM / yyyy",
                            Locale.getDefault()
                        )

                        dob = formatter.format(Date(it))
                    }

                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text("Cancel")
                }
            }

        ) {

            DatePicker(state = datePickerState)
        }
    }

    /* ================= ROOT ================= */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        /* ================= HEADER ================= */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFFFACF69))
        ) {

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(12.dp)
            ) {

                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color(0xFF7A9B00)
                )
            }

            Text(
                text = "Edit Profile",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),

                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A9B00),
                style = MaterialTheme.typography.titleLarge
            )

            /* ===== Avatar ===== */

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF7A9B00),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .offset((-12).dp, (-12).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8F9B20))
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        /* ================= FORM ================= */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            EditField("Name", name) { name = it }

            EditField("Email Address", email) { email = it }

            EditField("Phone Number", phone) {

                phone = it
                    .filter { c -> c.isDigit() }
                    .take(13)
            }

            DropdownField(
                label = "Gender",
                selectedValue = gender,
                options = genderOptions
            ) {
                gender = it
            }

            DatePickerField("Birth Of Date", dob) {
                showDatePicker = true
            }


            DropdownField("Job", job, jobOptions) {
                job = it
            }

            DropdownField("Income", income, incomeOptions) {
                income = it
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    showSaveDialog = true
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),

                shape = FieldShape,

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFACF69)
                )
            ) {

                Text("Save", color = Color.Black)
            }
        }
    }
}

/* ================= EDIT FIELD ================= */

@Composable
private fun EditField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {

    Column(Modifier.fillMaxWidth()) {

        Text(
            "$label:",
            color = Color(0xFF7A9B00),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(

            value = value,
            onValueChange = onChange,

            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight),

            trailingIcon = {

                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF7A9B00)
                )
            },

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

/* ================= DROPDOWN ================= */

@Composable
private fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {

        Text(
            "$label:",
            color = Color(0xFF7A9B00),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        Box {

            Card(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight)
                    .clickable { expanded = true },

                shape = FieldShape,

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = selectedValue.ifEmpty {
                            "Select $label"
                        },

                        modifier = Modifier.weight(1f),

                        color =
                            if (selectedValue.isEmpty())
                                Color.Gray
                            else
                                Color.Black
                    )

                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF7A9B00)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                options.forEach {

                    DropdownMenuItem(
                        text = { Text(it) },

                        onClick = {

                            onSelected(it)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/* ================= DATE ================= */

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit
) {

    Column(Modifier.fillMaxWidth()) {

        Text(
            "$label:",
            color = Color(0xFF7A9B00),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(

            value = value,
            onValueChange = { },

            enabled = false,

            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight)
                .clickable { onClick() },

            trailingIcon = {

                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFF7A9B00)
                )
            },

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
