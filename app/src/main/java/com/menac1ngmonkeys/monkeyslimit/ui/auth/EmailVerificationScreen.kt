package com.menac1ngmonkeys.monkeyslimit.ui.auth

import AppViewModelProvider
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    email: String, // Pass the email so we can show it to the user
    onVerificationSuccess: () -> Unit,
    onCancelRegistration: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val authUiState by authViewModel.uiState.collectAsState()

    EmailVerificationContent(
        email = email,
        isWaitingForVerification = authUiState.isWaitingForVerification,
        error = authUiState.error,
        onResendEmail = { onRateLimitHit ->
            authViewModel.resendVerificationEmail(onRateLimitHit)
        },
        onCancelVerification = {
            authViewModel.cancelVerification()
        },
        onClearError = {
            authViewModel.clearError()
        },
        onVerificationSuccess = onVerificationSuccess,
        onCancelRegistration = onCancelRegistration
    )
}

@Composable
fun EmailVerificationContent(
    email: String,
    isWaitingForVerification: Boolean,
    error: String?,
    onResendEmail: (onRateLimitHit: () -> Unit) -> Unit,
    onCancelVerification: () -> Unit,
    onClearError: () -> Unit,
    onVerificationSuccess: () -> Unit,
    onCancelRegistration: () -> Unit,
) {
    val context = LocalContext.current

    var cooldown by remember { mutableIntStateOf(60) }

    // 1. Handle the 60-second countdown timer
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown--
        }
    }

    // 2. Listen for the background polling success
    LaunchedEffect(isWaitingForVerification, error) {
        if (!isWaitingForVerification && error?.startsWith("Success") == true) {
            Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_LONG).show()
            onClearError()
            onVerificationSuccess()
        }
    }

    // 3. Prevent the hardware back button from breaking the flow
    BackHandler {
        onCancelVerification()
        onCancelRegistration()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AuthPrimaryGreen)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(start = 24.dp, top = 60.dp)
            ) {
                Column {
                    Text(
                        text = "Verify Email",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Check your inbox",
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
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = "Email Sent",
                        tint = AuthPrimaryGreen,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "We sent a verification link to:",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = email,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    Text(
                        text = "Please open your email app and click the link. This screen will automatically update once verified.",
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Resend Button
                    Button(
                        onClick = {
                            cooldown = 60
                            onResendEmail {
                                Toast.makeText(context, "Too many requests. Please wait.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = cooldown == 0,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuthPrimaryYellow,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Text(
                            text = if (cooldown > 0) "Resend in ${cooldown}s" else "Resend Email",
                            color = if (cooldown > 0) Color.DarkGray else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cancel Button
                    TextButton(
                        onClick = {
                            onCancelVerification()
                            onCancelRegistration()
                        }
                    ) {
                        Text("Cancel Registration", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
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
fun EmailVerificationScreenPreview() {
    MonkeyslimitTheme {
        EmailVerificationContent(
            email = "user@example.com",
            isWaitingForVerification = true,
            error = null,
            onResendEmail = {},
            onCancelVerification = {},
            onClearError = {},
            onVerificationSuccess = {},
            onCancelRegistration = {}
        )
    }
}
