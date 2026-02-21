package com.menac1ngmonkeys.monkeyslimit.ui.auth

import AppViewModelProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import java.util.*

// Helper List for Income
val incomeOptions = listOf(
    "< Rp 1.000.000",
    "Rp 1.000.000 - Rp 3.000.000",
    "Rp 3.000.000 - Rp 5.000.000",
    "Rp 5.000.000 - Rp 8.000.000",
    "Rp 8.000.000 - Rp 12.000.000",
    "Rp 12.000.000 - Rp 20.000.000",
    "> Rp 20.000.000"
)

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
    onEmailSignUp: (String, String, String, String, String, String, Date?, String, String, Boolean) -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val authUiState by authViewModel.uiState.collectAsState()

    SignUpContent(
        isLoading = authUiState.isLoading,
        error = authUiState.error,
        isWaitingForVerification = authUiState.isWaitingForVerification,
        onClearError = { authViewModel.clearError() },
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToVerification = onNavigateToVerification,
        onEmailSignUp = onEmailSignUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    isLoading: Boolean,
    error: String?,
    isWaitingForVerification: Boolean,
    onClearError: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
    onEmailSignUp: (String, String, String, String, String, String, Date?, String, String, Boolean) -> Unit,
) {
    // Form States
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") }
    var birthDateObject by remember { mutableStateOf<Date?>(null) }
    var gender by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var marriageStatusStr by remember { mutableStateOf("") } // "Married" or "Single"

    // --- Password States ---
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validation State (For Pop-up Dialog)
    var validationError by remember { mutableStateOf<String?>(null) }

    // Field-specific Error States (For the red text under fields)
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var jobError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var incomeError by remember { mutableStateOf<String?>(null) }
    var marriageStatusError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Dropdown States
    var jobExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var incomeExpanded by remember { mutableStateOf(false) }
    var marriedExpanded by remember { mutableStateOf(false) }

    // Date Picker Logic
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            override fun isSelectableYear(year: Int) = year <= Calendar.getInstance().get(Calendar.YEAR)
        }
    )

    if (showDatePicker) {
        AuthDatePickerDialog(
            state = datePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = { obj, text ->
                birthDateObject = obj
                birthDateText = text
                birthDateError = null // Clear error when valid date selected
            }
        )
    }

    // COMBINED ERROR DIALOG (Handles both Firebase errors & Local Validation errors)
    val activeError = error ?: validationError
    if (activeError != null && !activeError.startsWith("Success")) {
        AlertDialog(
            onDismissRequest = {
                onClearError()
                validationError = null
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = if (activeError.startsWith("Success")) "Check Your Email"
                    else if (validationError != null) "Input Required"
                    else "Registration Error",
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
                    onClick = {
                        // 1. Check if the current message is our success message
                        val isSuccess = activeError.startsWith("Success")

                        // 2. Clear the errors to dismiss the dialog
                        onClearError()
                        validationError = null

                        // 3. If it was successful, navigate back to Login!
                        if (isSuccess) {
                            onNavigateToLogin()
                        }
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryYellow),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Trigger navigation when the ViewModel successfully starts the verification process
    LaunchedEffect(isWaitingForVerification) {
        if (isWaitingForVerification) {
            onNavigateToVerification(email)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(AuthPrimaryGreen)) {
            // Header
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(start = 24.dp, top = 60.dp)) {
                Column {
                    Text("Sign Up", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("For Your Account", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(40.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuthInputField(
                            label = "First Name",
                            value = firstName,
                            onValueChange = { firstName = it; firstNameError = null },
                            modifier = Modifier.weight(1f),
                            isError = firstNameError != null,
                            errorMessage = firstNameError
                        )
                        AuthInputField(
                            label = "Last Name",
                            value = lastName,
                            onValueChange = { lastName = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AuthInputField(
                        label = "Email Address",
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        isError = emailError != null,
                        errorMessage = emailError
                    )

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

                    // Birth Date
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

                    // Income Dropdown
                    AuthDropdownField(
                        label = "Monthly Income",
                        selectedValue = income,
                        options = incomeOptions,
                        expanded = incomeExpanded,
                        onExpandedChange = { incomeExpanded = it },
                        onSelect = { income = it; incomeError = null },
                        isError = incomeError != null,
                        errorMessage = incomeError
                    )

                    // Marriage Status Dropdown
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

                    AuthInputField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        isError = passwordError != null,
                        errorMessage = passwordError
                    )

                    AuthInputField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = null },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            var hasError = false
                            val indoPhoneRegex = "^(?:\\+62|62|0)8[0-9]{8,11}$".toRegex()

                            // 1. Check Name
                            if (firstName.isBlank()) {
                                firstNameError = "Required"
                                hasError = true
                            } else if (firstName.trim().length < 3) {
                                firstNameError = "Min 3 chars"
                                hasError = true
                            }

                            // 2. Check Email
                            if (email.isBlank()) {
                                emailError = "Required"
                                hasError = true
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Invalid email format"
                                hasError = true
                            }

                            // 3. Check Mobile
                            if (mobile.isBlank()) {
                                mobileError = "Required"
                                hasError = true
                            } else if (!indoPhoneRegex.matches(mobile)) {
                                mobileError = "Invalid format (e.g., 08... or +628...)"
                                hasError = true
                            }

                            // 4. Check Dropdowns & Date
                            if (job.isBlank()) { jobError = "Required"; hasError = true }
                            if (birthDateObject == null) { birthDateError = "Required"; hasError = true }
                            if (gender.isBlank()) { genderError = "Required"; hasError = true }
                            if (income.isBlank()) { incomeError = "Required"; hasError = true }
                            if (marriageStatusStr.isBlank()) { marriageStatusError = "Required"; hasError = true }

                            // 5. Check Password (Gold Standard)
                            if (password.isBlank()) {
                                passwordError = "Required"
                                hasError = true
                            } else if (password.length < 8) {
                                passwordError = "Min 8 characters required"
                                hasError = true
                            } else if (!password.any { it.isUpperCase() }) {
                                passwordError = "Needs at least 1 uppercase letter"
                                hasError = true
                            } else if (!password.any { it.isLowerCase() }) {
                                passwordError = "Needs at least 1 lowercase letter"
                                hasError = true
                            } else if (!password.any { it.isDigit() }) {
                                passwordError = "Needs at least 1 number"
                                hasError = true
                            } else if (!password.any { !it.isLetterOrDigit() }) {
                                passwordError = "Needs at least 1 special character"
                                hasError = true
                            }

                            // 6. Check Confirm Password Match
                            if (confirmPassword.isBlank()) {
                                confirmPasswordError = "Required"
                                hasError = true
                            } else if (confirmPassword != password) {
                                confirmPasswordError = "Passwords do not match"
                                hasError = true
                            }

                            // FINISH: If any field had an error, show the dialog. Otherwise, save!
                            if (hasError) {
                                validationError = "Please check the highlighted fields below."
                            } else {
                                // ALL VALID -> Proceed with Registration
                                val isMarriedBool = marriageStatusStr == "Married"
                                onEmailSignUp(
                                    email, password, firstName, lastName,
                                    mobile, job, birthDateObject, gender,
                                    income, isMarriedBool
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryYellow),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(24.dp), Color.Black)
                        } else {
                            Text("Register", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Already Have An Account? Log In",
                        modifier = Modifier
                            .clickable { onNavigateToLogin() }
                            .padding(bottom = 24.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.logo_monkeys_limit),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(170.dp)
                .offset(y = 50.dp)
                .scale(scaleX = -1f, scaleY = 1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    MonkeyslimitTheme {
        SignUpContent(
            isLoading = false,
            error = null,
            isWaitingForVerification = false,
            onClearError = {},
            onNavigateToLogin = {},
            onNavigateToVerification = {},
            onEmailSignUp = { _, _, _, _, _, _, _, _, _, _ -> }
        )
    }
}
