package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import AppViewModelProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.viewmodel.SelectMemberViewModel

@Composable
fun SelectMemberScreen(
    excludedNames: List<String>,
    onNavigateBack: () -> Unit,
    // Callback now passes Members objects, not just strings
    onSelectionConfirmed: (ArrayList<Members>) -> Unit,
    viewModel: SelectMemberViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Members?>(null) }
    var preFilledName by remember { mutableStateOf("") }

    LaunchedEffect(excludedNames) {
        viewModel.setExcludedMembers(excludedNames)
    }

    if (showDialog) {
        AddMemberDialog(
            isEditMode = memberToEdit != null,
            initialName = preFilledName.ifBlank { memberToEdit?.name ?: "" },
            initialPhone = memberToEdit?.contact ?: "",
            initialNote = memberToEdit?.note ?: "",
            onDismiss = {
                showDialog = false
                memberToEdit = null
                preFilledName = ""
            },
            onConfirm = { name, phone, note ->
                if (memberToEdit != null) {
                    viewModel.updateMemberInDb(memberToEdit!!, name, phone, note)
                } else {
                    viewModel.addNewMemberToDb(name, phone, note)
                }
                showDialog = false
                memberToEdit = null
                preFilledName = ""
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            memberToEdit = null
                            preFilledName = ""
                            showDialog = true
                        },
                        modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, "New", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        },
        floatingActionButton = {
            if (uiState.selectedMembers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onSelectionConfirmed(ArrayList(uiState.selectedMembers)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Check, "Confirm")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "Add [Search Query]" Option -> Opens Dialog
            if (uiState.searchQuery.isNotEmpty() && !uiState.isSearchQueryInList) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable {
                                memberToEdit = null
                                preFilledName = uiState.searchQuery
                                showDialog = true
                            }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("Add \"${uiState.searchQuery}\"", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                }
            }

            if (uiState.filteredMembers.isEmpty() && uiState.searchQuery.isEmpty()) {
                item { Text("No contacts found.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
            }

            items(uiState.filteredMembers) { member ->
                val isSelected = uiState.selectedMembers.any { it.id == member.id && it.name == member.name }
                ContactRow(
                    member = member,
                    isSelected = isSelected,
                    onToggle = { viewModel.toggleSelection(member) },
                    onEdit = {
                        memberToEdit = member
                        showDialog = true
                    }
                )
            }
        }
    }
}

// Reusing your Dialog Style
@Composable
fun AddMemberDialog(
    isEditMode: Boolean,
    initialName: String,
    initialPhone: String,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var note by remember { mutableStateOf(initialNote) }

    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }
    var phoneMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(isEditMode) "Edit Member" else "Add New Member", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; isNameError = false },
                    label = { Text("Name") }, isError = isNameError, singleLine = true,
                    supportingText = if (isNameError) { { Text("Required") } } else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it; isPhoneError = false },
                    label = { Text("Phone Number") }, isError = isPhoneError, singleLine = true,
                    supportingText = if (isPhoneError) { { Text(phoneMsg) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Note (Optional)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cleanPhone = phone.trim()
                // Minimal validation: Numeric and length
                val isValidFormat = cleanPhone.all { it.isDigit() || it == '+' } && cleanPhone.length >= 10

                if (name.isNotBlank() && isValidFormat) {
                    onConfirm(name, cleanPhone, note)
                } else {
                    if (name.isBlank()) isNameError = true
                    if (!isValidFormat) {
                        isPhoneError = true
                        phoneMsg = "Invalid format (min 10 digits)"
                    }
                }
            }) { Text(if(isEditMode) "Save" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ContactRow(member: Members, isSelected: Boolean, onToggle: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Text(member.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (!member.contact.isNullOrBlank()) Text(member.contact, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (member.id != -1) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
    }
}