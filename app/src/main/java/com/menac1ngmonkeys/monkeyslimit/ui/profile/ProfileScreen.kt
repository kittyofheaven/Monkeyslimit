package com.menac1ngmonkeys.monkeyslimit.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileUiState
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AuthViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val uiState by profileViewModel.uiState.collectAsState()

    var darkMode by remember { mutableStateOf(false) }
    var notification by remember { mutableStateOf(true) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text("Logout")
            },
            text = {
                Text("Are you sure you want to log out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
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
            checked = notification,
            onCheckedChange = { notification = it }
        )

        Spacer(Modifier.height(12.dp))

        SettingItem(
            icon = Icons.Default.Inbox,
            title = "Inbox",
            onClick = { }
        )

        Spacer(Modifier.height(12.dp))

        SettingItem(
            icon = Icons.Default.Help,
            title = "FAQ",
            onClick = { }
        )

        Spacer(Modifier.height(12.dp))

        SettingItem(
            icon = Icons.Default.Logout,
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
private fun ProfileHeader(uiState: ProfileUiState, onEditClick: () -> Unit) {

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

                Row(

                ) {
                    Text(
                        uiState.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(3.dp))

                Row(

                ){
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
