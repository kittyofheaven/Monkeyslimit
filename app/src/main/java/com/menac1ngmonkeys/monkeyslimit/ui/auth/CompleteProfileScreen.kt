package com.menac1ngmonkeys.monkeyslimit.ui.auth

import AppViewModelProvider
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    onComplete: () -> Unit, // (This arg might be redundant if VM handles nav via state, but keeping signature)
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var mobile by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") }
    var birthDateObject by remember { mutableStateOf<Date?>(null) }
    var gender by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var marriageStatusStr by remember { mutableStateOf("") }

    // Validation State (For Pop-up Dialog)
    var validationError by remember { mutableStateOf<String?>(null) }

    // Field-specific Error States (For the red text under fields)
    var mobileError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var incomeError by remember { mutableStateOf<String?>(null) }
    var marriageStatusError by remember { mutableStateOf<String?>(null) }

    val authUiState by authViewModel.uiState.collectAsState()

    // Handle Back Press
    BackHandler {
        authViewModel.signOut()
    }

    // Dropdown States
    var jobExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var incomeExpanded by remember { mutableStateOf(false) }
    var marriedExpanded by remember { mutableStateOf(false) }

    // Date Picker
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            override fun isSelectableYear(year: Int) = year <= Calendar.getInstance().get(Calendar.YEAR)
        }
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AuthDatePickerDialog(datePickerState, { showDatePicker = false }, { obj, text ->
            birthDateObject = obj
            birthDateText = text
            birthDateError = null // Clear error when valid date selected
        })
    }

    val activeError = authUiState.error ?: validationError
    if (activeError != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError(); validationError = null },
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
                    text = activeError,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { authViewModel.clearError(); validationError = null },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryYellow),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(AuthPrimaryGreen)) {
            Box(Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 24.dp, top = 60.dp)) {
                Column {
                    Text(
                        text = "Almost There",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Complete your profile",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(40.dp))

                    AuthInputField(
                        label = "Mobile Number",
                        value = mobile,
                        onValueChange = { mobile = it; mobileError = null },
                        isError = mobileError != null,
                        errorMessage = mobileError
                    )

                    AuthDropdownField(
                        label = "Job",
                        selectedValue = job,
                        options = listOf("Student", "Employee", "Part-time Employee", "Entrepreneur", "Freelancer", "Housewife/househusband", "Other"),
                        expanded = jobExpanded,
                        onExpandedChange = { jobExpanded = it },
                        onSelect = { job = it; jobError = null },
                        isError = jobError != null,
                        errorMessage = jobError
                    )

                    Column(Modifier.fillMaxWidth()) {
                        Text("Birth Date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box(Modifier.fillMaxWidth()) {
                            AuthInputField(
                                label = "",
                                value = birthDateText,
                                onValueChange = {},
                                readOnly = true,
                                isError = birthDateError != null,
                                errorMessage = birthDateError
                            )
                            Box(Modifier.matchParentSize().clickable { showDatePicker = true })
                        }
                    }

                    AuthDropdownField(
                        label = "Gender",
                        selectedValue = gender,
                        options = listOf("Male", "Female"),
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it },
                        onSelect = { gender = it; genderError = null },
                        isError = genderError != null,
                        errorMessage = genderError
                    )

                    // Income
                    AuthDropdownField(
                        label = "Monthly Income",
                        selectedValue = income,
                        options = incomeOptions, // Reusing list from SignUpScreen file if available, or duplicate list
                        expanded = incomeExpanded,
                        onExpandedChange = { incomeExpanded = it },
                        onSelect = { income = it; incomeError = null },
                        isError = incomeError != null,
                        errorMessage = incomeError
                    )

                    // Marriage Status
                    AuthDropdownField(
                        label = "Marriage Status",
                        selectedValue = marriageStatusStr,
                        options = listOf("Single", "Married"),
                        expanded = marriedExpanded,
                        onExpandedChange = { marriedExpanded = it },
                        onSelect = { marriageStatusStr = it; marriageStatusError = null },
                        isError = marriageStatusError != null,
                        errorMessage = marriageStatusError
                    )

                    Spacer(Modifier.height(20.dp))

                    // FINISH BUTTON
                    Button(
                        onClick = {
                            var hasError = false
                            val indoPhoneRegex = "^(?:\\+62|62|0)8[0-9]{8,11}$".toRegex()

                            // FULL VALIDATION BLOCK (Checks everything at once)
                            if (mobile.isBlank()) {
                                mobileError = "Required"
                                hasError = true
                            } else if (!indoPhoneRegex.matches(mobile)) {
                                mobileError = "Invalid format (e.g., 08... or +628...)"
                                hasError = true
                            }

                            if (job.isBlank()) { jobError = "Required"; hasError = true }
                            if (birthDateObject == null) { birthDateError = "Required"; hasError = true }
                            if (gender.isBlank()) { genderError = "Required"; hasError = true }
                            if (income.isBlank()) { incomeError = "Required"; hasError = true }
                            if (marriageStatusStr.isBlank()) { marriageStatusError = "Required"; hasError = true }

                            if (hasError) {
                                validationError = "Please check the highlighted fields below."
                            } else {
                                val isMarriedBool = marriageStatusStr == "Married"
                                authViewModel.completeGoogleProfile(
                                    mobile, job, birthDateObject, gender,
                                    income, isMarriedBool
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryYellow),
                        enabled = !authUiState.isLoading
                    ) {
                        if (authUiState.isLoading) CircularProgressIndicator(Modifier.size(24.dp), Color.Black)
                        else Text("Finish Setup", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { authViewModel.signOut() },
                        enabled = !authUiState.isLoading
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.logo_monkeys_limit),
            contentDescription = null,
            Modifier
                .align(Alignment.TopEnd)
                .size(170.dp)
                .offset(y = 50.dp)
                .scale(scaleX = -1f, scaleY = 1f))
    }
}