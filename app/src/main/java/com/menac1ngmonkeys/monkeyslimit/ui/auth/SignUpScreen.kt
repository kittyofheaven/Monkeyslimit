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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onEmailSignUp: (String, String, String, String, String, String, Date?, String) -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoading = authUiState.isLoading

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") }
    var birthDateObject by remember { mutableStateOf<Date?>(null) }
    var gender by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
            override fun isSelectableYear(year: Int) = year <= Calendar.getInstance().get(Calendar.YEAR)
        }
    )

    var jobExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

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
            title = { Text("Registration Error") },
            text = { Text(authUiState.error ?: "") },
            confirmButton = { TextButton(onClick = { authViewModel.clearError() }) { Text("OK") } }
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
                        options = listOf(
                            "Student",
                            "Employee",
                            "Part-time Employee",
                            "Entrepreneur",
                            "Freelancer",
                            "Housewife/househusband",
                            "Other"
                        ),
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
                        onClick = { onEmailSignUp(email, password, firstName, lastName, mobile, job, birthDateObject, gender) },
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
        // Logo
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

@Composable
fun signUpTextFieldColors(cardBg: Color, primaryGreen: Color) = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = cardBg,
    focusedContainerColor = cardBg,
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = primaryGreen,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black
)

@Composable
fun CustomSignUpField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = signUpTextFieldColors(Color(0xFFF9F9F9), Color(0xFF6C8B08))
        )
    }
}

@Preview
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigateToLogin = {},
        onEmailSignUp = { _, _, _, _, _, _, _, _ -> }
    )
}