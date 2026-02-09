package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import AppViewModelProvider
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftItem
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftMember
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitDraft
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitItemUi
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ReviewSmartSplitViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSmartSplitScreen(
    imageUri: String?,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    viewModel: ReviewSmartSplitViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- ADDED: Error Handling ---
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearError()

            // Optional: If you want to go back automatically on invalid bill:
            // if (msg == "Not a valid bill") onNavigateBack()
        }
    }

    // FIX: Only trigger scan if the image is NEW (different from what's already in ViewModel)
    LaunchedEffect(imageUri) {
        viewModel.checkBackendHealth()

        if (imageUri != null && imageUri != uiState.imageUri) {
            // 1. Update the state with the new URI
            viewModel.setImageUri(imageUri)

            // 2. Perform the scan
            viewModel.scanReceipt(context, Uri.parse(imageUri))
        }
    }

    // Navigation Listener
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        // Observe DraftMember list instead of String list
        savedStateHandle?.getLiveData<ArrayList<DraftMember>>("selected_members")?.observeForever { draftMembers ->
            if (draftMembers.isNotEmpty()) {
                viewModel.addMembers(draftMembers)
                savedStateHandle.remove<ArrayList<DraftMember>>("selected_members")
            }
        }
    }

    // Local States
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<SmartSplitItemUi?>(null) }
    var isImageExpanded by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- DIALOGS ---
    if (isImageExpanded && uiState.imageUri != null) {
        Dialog(onDismissRequest = { isImageExpanded = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { isImageExpanded = false }) {
                AsyncImage(model = uiState.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
        }
    }

    if (showAddDialog || itemToEdit != null) {
        SmartSplitItemDialog(
            title = if (itemToEdit != null) "Edit Item" else "Add Item",
            initialName = itemToEdit?.name ?: "",
            initialPrice = itemToEdit?.price ?: 0.0,
            initialQty = itemToEdit?.quantity ?: 1,
            onDismiss = { showAddDialog = false; itemToEdit = null },
            onConfirm = { name, price, qty ->
                if (itemToEdit != null) {
                    viewModel.updateItem(itemToEdit!!.id, name, price, qty)
                    itemToEdit = null
                } else {
                    viewModel.addItem(name, price, qty)
                    showAddDialog = false
                }
            }
        )
    }

    // --- BOTTOM SHEET (Extra Charges) ---
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Others", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                SplitSummaryInputRow("Discount", if (uiState.discount > 0) uiState.discount.toString() else "") { viewModel.updateDiscount(it.toDoubleOrNull() ?: 0.0) }
                Spacer(Modifier.height(16.dp))
                SplitSummaryInputRow("Tax", if (uiState.tax > 0) uiState.tax.toString() else "") { viewModel.updateTax(it.toDoubleOrNull() ?: 0.0) }
                Spacer(Modifier.height(16.dp))
                SplitSummaryInputRow("Service", if (uiState.service > 0) uiState.service.toString() else "") { viewModel.updateService(it.toDoubleOrNull() ?: 0.0) }
                Spacer(Modifier.height(16.dp))
                SplitSummaryInputRow("Others", if (uiState.others > 0) uiState.others.toString() else "") { viewModel.updateOthers(it.toDoubleOrNull() ?: 0.0) }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Done", fontWeight = FontWeight.Bold) }
            }
        }
    }

    // --- MAIN CONTENT ---
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ... (Image, Header, Bill Name, Friends, Items code remains same) ...
                // 1. IMAGE PREVIEW
                if (uiState.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { isImageExpanded = true }
                    ) {
                        AsyncImage(model = uiState.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }

                // 2. HEADER
                Text("Review Bill", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                // BILL NAME
                Text("Bill Name", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextField(
                    value = uiState.splitName,
                    onValueChange = { viewModel.setSplitName(it) },
                    placeholder = { Text("e.g Team Lunch") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(24.dp))

                // 3. FRIENDS SELECTOR
                Text("Select friend to assign items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        // --- FIX IS HERE: ENCODE THE NAMES TO HANDLE SPACES ---
                                        val currentNames = uiState.availableMembers.joinToString(",") { it.name }
                                        val encodedNames = Uri.encode(currentNames)
                                        val route = NavItem.SelectMember.route.replace("{exclude}", encodedNames)
                                        navController.navigate(route)
                                    },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Add, "Add Friend", tint = MaterialTheme.colorScheme.onSurface) }
                            Spacer(Modifier.height(4.dp))
                            Text("Add", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    items(uiState.availableMembers) { member ->
                        val isSelected = member.id == uiState.selectedMemberIdForAssignment
                        HighlightableMemberAvatar(name = member.name, isSelected = isSelected, onClick = { viewModel.selectMemberForAssignment(member.id) }, onRemove = { viewModel.removeMember(member.id) })
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                // 4. ITEM LIST HEADER + SPLIT EVENLY TOGGLE ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Item List",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Split the bill evenly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = uiState.isSplitEvenly,
                            onCheckedChange = { viewModel.setSplitEvenly(it) },
                            colors = SwitchDefaults.colors(
                                // Using a yellowish/olive color to mimic the image provided,
                                // or rely on default primary if preferred.
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                // ---------------------------------------------------

                // 5. ITEMS LIST
                uiState.items.forEachIndexed { index, item ->
                    SmartSplitItemRow(
                        item = item,
                        availableMembers = uiState.availableMembers,
                        onEdit = { itemToEdit = item },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onRowClick = {
                            uiState.selectedMemberIdForAssignment?.let { memberId -> viewModel.toggleMemberAssignment(item.id, memberId) }
                        }
                    )
                    if (index < uiState.items.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))
                }
                Spacer(Modifier.height(16.dp))

                // 6. ADD ITEM BUTTON
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showAddDialog = true }.padding(vertical = 8.dp)) {
                    Icon(Icons.Default.AddCircleOutline, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 16.dp))

                // 7. SUMMARY SECTION (UPDATED)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryRow("Subtotal", uiState.subtotal)

                    // Header for Extras
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Others", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        // Net value of extras (Additions - Deductions)
                        val netExtras = uiState.tax + uiState.service + uiState.others - uiState.discount
                        Text(formatCurrency(netExtras), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { showBottomSheet = true }, modifier = Modifier.size(32.dp).padding(start = 4.dp)) {
                            Icon(Icons.Default.Edit, "Edit Extras", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Details
                    SummaryRow("Discount", -uiState.discount, isDetail = true, isNegative = true)
                    SummaryRow("Service Charge", uiState.service, isDetail = true)
                    SummaryRow("Tax", uiState.tax, isDetail = true)
                    SummaryRow("Others", uiState.others, isDetail = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(formatCurrency(uiState.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        // 1. Generate Draft Data from ViewModel
                        val draft = viewModel.createDraft()

                        // 2. Pass Draft to next screen
                        navController.currentBackStackEntry?.savedStateHandle?.set("split_draft", draft)

                        // 3. Navigate to Result
                        navController.navigate(NavItem.SplitResult.route)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = uiState.items.isNotEmpty() && uiState.splitName.isNotBlank()
                ) { Text("Preview Split") }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---

@Composable
fun SummaryRow(label: String, amount: Double, isDetail: Boolean = false, isNegative: Boolean = false) {

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isDetail) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            color = if (isDetail) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = if (isDetail) Modifier.padding(start = 16.dp) else Modifier
        )
        Text(
            text = formatCurrency(amount),
            style = if (isDetail) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isDetail) FontWeight.Normal else FontWeight.SemiBold,
            color = if (isNegative) MaterialTheme.colorScheme.error else if (isDetail) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HighlightableMemberAvatar(name: String, isSelected: Boolean, onClick: () -> Unit, onRemove: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp, end = 4.dp)) {
            Box(modifier = Modifier.size(56.dp).then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier).padding(if (isSelected) 3.dp else 0.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).clickable { onClick() }, contentAlignment = Alignment.Center) {
                Text(name.take(2).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(4.dp))
            Text(name, style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.widthIn(max = 70.dp), textAlign = TextAlign.Center, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
        Box(modifier = Modifier.size(20.dp).background(MaterialTheme.colorScheme.error, CircleShape).border(1.dp, MaterialTheme.colorScheme.surface, CircleShape).clickable { onRemove() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(14.dp)) }
    }
}

@Composable
fun SmartSplitItemRow(item: SmartSplitItemUi, availableMembers: List<Members>, onEdit: () -> Unit, onDelete: () -> Unit, onRowClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onRowClick() }.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.assignedMemberIds.isEmpty()) Text("Tap to assign...", style = MaterialTheme.typography.bodySmall)
            else {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    item.assignedMemberIds.take(5).forEach { id ->
                        val m = availableMembers.find { it.id == id }
                        Box(modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).border(1.dp, MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) {
                            Text(m?.name?.take(1)?.uppercase() ?: "?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    if (item.assignedMemberIds.size > 5) {
                        Box(modifier = Modifier.size(28.dp).background(Color.Gray, CircleShape).border(1.dp, MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) { Text("+${item.assignedMemberIds.size - 5}", fontSize = 10.sp, color = Color.White) }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("x${item.quantity}", style = MaterialTheme.typography.bodySmall)
                Text(formatCurrency(item.price), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SplitSummaryInputRow(label: String, value: String, onValueChange: (String) -> Unit) {
    val bottomLineColor = MaterialTheme.colorScheme.outlineVariant

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Box(
            modifier = Modifier
                .width(100.dp)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = bottomLineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(bottom = 8.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if(value.isEmpty()) Text("0", color = Color.Gray, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun SmartSplitItemDialog(title: String, initialName: String, initialPrice: Double, initialQty: Int, onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var priceStr by remember { mutableStateOf(if (initialPrice > 0) initialPrice.toString() else "") }
    var qtyStr by remember { mutableStateOf(if (initialQty > 0) initialQty.toString() else "") }
    var isNameError by remember { mutableStateOf(false) }
    var isPriceError by remember { mutableStateOf(false) }
    var isQtyError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it; isNameError = false }, label = { Text("Item Name") }, singleLine = true, isError = isNameError, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary, cursorColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it; isPriceError = false }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, isError = isPriceError, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary, cursorColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = qtyStr, onValueChange = { qtyStr = it; isQtyError = false }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, isError = isQtyError, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary, cursorColor = MaterialTheme.colorScheme.primary))
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalPrice = priceStr.toDoubleOrNull() ?: 0.0; val finalQty = qtyStr.toIntOrNull() ?: 0; val nameValid = name.isNotBlank(); val priceValid = finalPrice > 0.0; val qtyValid = finalQty > 0
                if (nameValid && priceValid && qtyValid) onConfirm(name, finalPrice, finalQty) else { isNameError = !nameValid; isPriceError = !priceValid; isQtyError = !qtyValid }
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text("Cancel") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

private fun formatCurrency(amount: Double): String = NumberFormat.getNumberInstance(Locale("in", "ID")).format(amount)