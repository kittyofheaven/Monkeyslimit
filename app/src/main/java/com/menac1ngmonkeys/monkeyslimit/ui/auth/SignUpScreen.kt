package com.menac1ngmonkeys.monkeyslimit.ui.auth

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    // UPDATED SIGNATURE
    onEmailSignUp: (String, String, String, String, String, String, Date?, String, String, Boolean) -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoading = authUiState.isLoading

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
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            }
        )
    }

    if (authUiState.error != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Registration Error",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = authUiState.error ?: "An unknown error occurred",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { authViewModel.clearError() },
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
                        AuthInputField("First Name", firstName, { firstName = it }, Modifier.weight(1f))
                        AuthInputField("Last Name", lastName, { lastName = it }, Modifier.weight(1f))
                    }

                    AuthInputField("Email Address", email, { email = it })
                    AuthInputField("Mobile Number", mobile, { mobile = it })

                    AuthDropdownField(
                        label = "Job",
                        selectedValue = job,
                        options = listOf("Student", "Employee", "Part-time Employee", "Entrepreneur", "Freelancer", "Housewife/househusband", "Other"),
                        expanded = jobExpanded,
                        onExpandedChange = { jobExpanded = it },
                        onSelect = { job = it }
                    )

                    // Birth Date
                    Column(Modifier.fillMaxWidth()) {
                        Text("Birth Date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box(Modifier.fillMaxWidth()) {
                            AuthInputField("", birthDateText, {}, enabled = false)
                            Box(Modifier.matchParentSize().clickable { showDatePicker = true })
                        }
                    }

                    AuthDropdownField("Gender", gender, listOf("Male", "Female"), genderExpanded, { genderExpanded = it }, { gender = it })

                    // NEW: Income Dropdown
                    AuthDropdownField(
                        label = "Monthly Income",
                        selectedValue = income,
                        options = incomeOptions,
                        expanded = incomeExpanded,
                        onExpandedChange = { incomeExpanded = it },
                        onSelect = { income = it }
                    )

                    // NEW: Marriage Status Dropdown
                    AuthDropdownField(
                        label = "Marriage Status",
                        selectedValue = marriageStatusStr,
                        options = listOf("Single", "Married"),
                        expanded = marriedExpanded,
                        onExpandedChange = { marriedExpanded = it },
                        onSelect = { marriageStatusStr = it }
                    )

                    AuthInputField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val isMarriedBool = marriageStatusStr == "Married"
                            onEmailSignUp(
                                email, password, firstName, lastName,
                                mobile, job, birthDateObject, gender,
                                income, isMarriedBool
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryYellow),
                        enabled = !isLoading
                    ) {
                        Text("Register", color = Color.Black, fontWeight = FontWeight.Bold)
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

@Preview
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigateToLogin = {},
        onEmailSignUp = { _, _, _, _, _, _, _, _, _, _ -> }
    )
}