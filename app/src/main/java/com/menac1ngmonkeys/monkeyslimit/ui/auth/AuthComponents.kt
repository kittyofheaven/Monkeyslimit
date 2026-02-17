package com.menac1ngmonkeys.monkeyslimit.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// --- Shared Constants ---
val AuthPrimaryGreen = Color(0xFF6C8B08)
val AuthPrimaryYellow = Color(0xFFF9E3B6)
val AuthCardBg = Color(0xFFF9F9F9)

val AuthPrimaryYellowDark = Color(0xFFFAC037)


// --- Shared TextField Colors ---
@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = AuthCardBg,
    focusedContainerColor = AuthCardBg,
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = AuthPrimaryGreen,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,
    disabledContainerColor = AuthCardBg,
    disabledBorderColor = Color.Transparent,
    disabledLabelColor = Color.Gray,
    disabledPlaceholderColor = Color.Gray
)

// --- Reusable Text Field with Label ---
@Composable
fun AuthInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(label),
            shape = RoundedCornerShape(25.dp),
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            colors = authTextFieldColors()
        )

        // NEW: Show error message below the field
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// --- Reusable Dropdown ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    ),
                shape = RoundedCornerShape(25.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = authTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// --- Reusable Date Picker Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDatePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Date, String) -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
                    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                    onConfirm(date, formatter.format(date))
                }
                onDismiss()
            }, colors = ButtonDefaults.textButtonColors(contentColor = AuthPrimaryGreen)) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) { Text("Cancel") }
        },
        colors = DatePickerDefaults.colors(containerColor = Color.White)
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                selectedDayContainerColor = AuthPrimaryGreen,
                selectedDayContentColor = Color.White,
                todayContentColor = AuthPrimaryGreen,
                todayDateBorderColor = AuthPrimaryGreen,
                titleContentColor = AuthPrimaryGreen,
                headlineContentColor = AuthPrimaryGreen,
                dateTextFieldColors = authTextFieldColors()
            )
        )
    }
}