package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import AppViewModelProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitDraft
import com.menac1ngmonkeys.monkeyslimit.viewmodel.MemberSplitDetail
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SplitResultViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SplitResultScreen(
    navController: NavController,
    onNavigateHome: () -> Unit,
    viewModel: SplitResultViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val draft = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<SmartSplitDraft>("split_draft")

        if (draft != null) {
            viewModel.initializeWithDraft(draft)
        }
    }

    // Use Box instead of Scaffold to remove reserved top bar space
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                // Add bottom padding so the last item isn't hidden behind the button
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(
                            text = uiState.draft?.splitName ?: "Bill Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap card to view details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                items(uiState.memberDetails) { memberDetail ->
                    MemberBillCard(memberDetail)
                }
            }
        }

        // Bottom Button aligned to the bottom of the Box
        Button(
            onClick = {
                viewModel.saveToDatabase(context, onSuccess = onNavigateHome)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm & Save")
            }
        }
    }
}

@Composable
fun MemberBillCard(detail: MemberSplitDetail) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = detail.memberName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(detail.memberName, fontWeight = FontWeight.Bold)
                    Text(
                        formatCurrency(detail.totalOwed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    "Expand",
                    modifier = Modifier.rotate(rotationState)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))

                    detail.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.itemName, fontSize = 14.sp)
                            Text(formatCurrency(item.splitPrice), fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))

                    if (detail.taxShare > 0) BreakdownRow("Tax", detail.taxShare)
                    if (detail.serviceShare > 0) BreakdownRow("Service", detail.serviceShare)
                    if (detail.othersShare > 0) BreakdownRow("Others", detail.othersShare)
                    if (detail.discountShare > 0) BreakdownRow("Discount", -detail.discountShare, true)
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(label: String, amount: Double, isNegative: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(
            formatCurrency(amount),
            fontSize = 12.sp,
            color = if (isNegative) MaterialTheme.colorScheme.error else Color.Gray
        )
    }
}

private fun formatCurrency(amount: Double): String = NumberFormat.getNumberInstance(Locale("in", "ID")).format(amount)