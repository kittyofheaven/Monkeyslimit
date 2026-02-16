package com.menac1ngmonkeys.monkeyslimit.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.Auth
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoading = authUiState.isLoading
    val context = LocalContext.current


    // --- State Management ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- Forgot Password State ---
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    // --- Error Dialog ---
    if (authUiState.error != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Authentication Error",
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

    // --- Forgot Password Dialog ---
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Reset Password",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your email address to receive a password reset link.",
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    AuthInputField(
                        label = "Email Address",
                        value = resetEmail,
                        onValueChange = { resetEmail = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.sendPasswordResetEmail(resetEmail) {
                            showForgotPasswordDialog = false
                            Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(50.dp),
                    enabled = !isLoading
                ) {
                    Text("Send", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false },
                    enabled = !isLoading
                ) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Root Box to handle Z-Index overlapping (Monkey Logo on top)
    Box(modifier = Modifier.fillMaxSize()) {

        // Bottom Layer: Background and Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AuthPrimaryYellow)
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(start = 24.dp, top = 60.dp)
            ) {
                Column {
                    Text(
                        text = "Log In",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthPrimaryGreen
                    )
                    Text(
                        text = "To Continue",
                        fontSize = 16.sp,
                        color = AuthPrimaryGreen.copy(alpha = 0.7f)
                    )
                }
            }

            // White Card Content
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Reusable Email Field
                    AuthInputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it }
                    )

                    // Reusable Password Field
                    AuthInputField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.Gray
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Action
                    Button(
                        onClick = { onEmailSignIn(email, password) },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryGreen),
                        enabled = !isLoading
                    ) {
                        Text("Log In", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Google Login button
                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = !isLoading
                    ) {
                        Text("Log In with Google", color = AuthPrimaryGreen)
                    }

                    Text(
                        text = "Forgot Password?",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable {
                            resetEmail = email // Auto-fill if they already typed something
                            showForgotPasswordDialog = true
                        }
                    )

                    Text(
                        text = "Don’t Have An Account? Sign Up Here!",
                        modifier = Modifier.clickable { onNavigateToSignUp() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (isLoading) {
                        CircularProgressIndicator(color = AuthPrimaryGreen)
                    }
                }
            }
        }

        // Top Layer: The Monkey Logo (overlapping the card)
        Image(
            painter = painterResource(id = R.drawable.logo_monkeys_limit),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(170.dp)
                .offset(y = 50.dp)
                .scale(scaleX = -1f, scaleY = 1f),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onGoogleSignIn = {},
        onEmailSignIn = { _, _ -> },
        onNavigateToSignUp = {}
    )
}