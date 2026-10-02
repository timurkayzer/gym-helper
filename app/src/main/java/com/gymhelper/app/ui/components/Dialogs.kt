package com.gymhelper.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun TextInputDialog(
    title: String,
    confirmLabel: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value.trim()) },
                enabled = value.isNotBlank(),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun NumberInputDialog(
    title: String,
    fields: List<NumberFieldSpec>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Int>) -> Unit,
) {
    val fieldStates = remember(fields) {
        fields.map { mutableStateOf(it.initial.toString()) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                fields.forEachIndexed { index, field ->
                    OutlinedTextField(
                        value = fieldStates[index].value,
                        onValueChange = { fieldStates[index].value = it },
                        label = { Text(field.label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = fields.mapIndexedNotNull { index, field ->
                        val number = fieldStates[index].value.toIntOrNull()
                        if (number == null || number < field.min) null else field.key to number
                    }.toMap()
                    if (parsed.size == fields.size) {
                        onConfirm(parsed)
                    }
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

data class NumberFieldSpec(
    val key: String,
    val label: String,
    val initial: Int,
    val min: Int = 0,
)

@Composable
fun IntervalExerciseDialog(
    title: String,
    defaultRoundSeconds: Int,
    initialName: String = "",
    initialCustomDuration: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, customDurationSeconds: Int?) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var customDuration by remember {
        mutableStateOf(initialCustomDuration?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = customDuration,
                    onValueChange = { customDuration = it },
                    label = { Text("Custom duration (seconds, optional)") },
                    placeholder = { Text("Default: ${defaultRoundSeconds}s") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = customDuration.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                    if (name.isNotBlank() && (customDuration.isBlank() || (duration != null && duration > 0))) {
                        onConfirm(name.trim(), duration)
                    }
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
