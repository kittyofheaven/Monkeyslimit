package com.menac1ngmonkeys.monkeyslimit.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.worker.NotificationHelper
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileAuthStatus
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileUiState
import com.menac1ngmonkeys.monkeyslimit.utils.BatteryUtils
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    val uiState by profileViewModel.uiState.collectAsState()

    var darkMode by remember { mutableStateOf(false) }
    val isNotificationEnabled = (uiState.status as? ProfileAuthStatus.Verified)?.user?.isNotificationEnabled ?: true
    val context = LocalContext.current // Get context for WorkManager

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                    },
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // --- BATTERY DIALOG ---
    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Background Reminders 🐒",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "To ensure the Monkeyslimit notification can work, please set MonkeysLimit to 'No restrictions' in your battery settings.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBatteryDialog = false
                        BatteryUtils.openBatteryOptimizationSettings(context)
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACF69)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Go to Settings", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryDialog = false }) {
                    Text("Later", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffffff))
            .padding(16.dp)
    ) {
        ProfileHeader(uiState = uiState, onEditClick = {
            navController.navigate("edit_profile")
        })

        Spacer(Modifier.height(24.dp))

        SettingToggle(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            checked = isNotificationEnabled,
            onCheckedChange = { isEnabled ->
                profileViewModel.toggleLocalNotifications(isEnabled)
                if (isEnabled) {
                    // Restart the reminders
                    NotificationHelper.scheduleDailyReminders(context)

                    // Check if battery is restricted. If yes, show the nice dialog!
                    if (!BatteryUtils.isBatteryOptimizationIgnored(context)) {
                        showBatteryDialog = true
                    }
                } else {
                    // Kill all running workers
                    NotificationHelper.cancelAllReminders(context)
                }
            }
        )

        Spacer(Modifier.height(12.dp))

//        SettingItem(
//            icon = Icons.Default.Inbox,
//            title = "Inbox",
//            onClick = { }
//        )
//
//        Spacer(Modifier.height(12.dp))
//
//        SettingItem(
//            icon = Icons.AutoMirrored.Filled.Help,
//            title = "FAQ",
//            onClick = { }
//        )
//
//        Spacer(Modifier.height(12.dp))

        SettingItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Log Out",
            onClick = {
                showLogoutDialog = true
            }
        )
    }
}

/* ---------------- TOP BAR ---------------- */

@Composable
private fun TopBar(onBack: () -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "My Profile",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF7A9B00)
        )
    }
}

/* ---------------- HEADER ---------------- */

@Composable
fun ProfileHeader(uiState: ProfileUiState, onEditClick: () -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
            ){
                AsyncImage(
                    model = uiState.photoUrl ?: R.drawable.account_circle_24dp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row{
                    Text(
                        uiState.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(3.dp))

                Row{
                    Text(
                        uiState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(3.dp))

                Box {

                    // Figma-style shadow

                    // Button
                    Button(
                        onClick = { onEditClick()},
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFACF69)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Edit Profile", color = Color.Black)
                    }
                }
            }
        }
    }
}

/* ---------------- TOGGLE ITEM ---------------- */

@Composable
private fun SettingToggle(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(icon, null, tint = Color(0xFFFFB300))

            Spacer(Modifier.width(16.dp))

            Text(
                title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/* ---------------- LIST ITEM ---------------- */

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(icon, null, tint = Color(0xFFFFB300))

            Spacer(Modifier.width(16.dp))

            Text(
                title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
