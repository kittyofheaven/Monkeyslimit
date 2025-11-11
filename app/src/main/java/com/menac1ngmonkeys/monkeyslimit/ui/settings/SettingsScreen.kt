package com.menac1ngmonkeys.monkeyslimit.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.data.local.seeders.DatabaseResetter
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isResetting) showConfirm = false },
            title = { Text("Reset Database") },
            text = { Text("Semua data akan dihapus dan categories diisi ulang. Lanjut?") },
            confirmButton = {
                TextButton(
                    enabled = !isResetting,
                    onClick = {
                        isResetting = true
                        scope.launch {
                            try {
                                DatabaseResetter.resetAndReseed(context)
                                Toast.makeText(context, "Database direset", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal reset: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isResetting = false
                                showConfirm = false
                            }
                        }
                    }
                ) { Text("Ya, reset") }
            },
            dismissButton = {
                TextButton(enabled = !isResetting, onClick = { showConfirm = false }) { Text("Batal") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { showConfirm = true }, enabled = !isResetting) {
            if (isResetting) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text("Reset Database")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
