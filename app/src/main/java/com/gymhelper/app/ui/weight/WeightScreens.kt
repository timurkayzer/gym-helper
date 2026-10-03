package com.gymhelper.app.ui.weight

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymhelper.app.GymHelperApp
import com.gymhelper.app.data.WeightDay
import com.gymhelper.app.data.WeightExercise
import com.gymhelper.app.data.WeightProgram
import com.gymhelper.app.ui.components.AppScaffold
import com.gymhelper.app.ui.components.ImportDaysDialog
import com.gymhelper.app.ui.components.ReorderableExerciseList
import com.gymhelper.app.ui.components.ReorderableTextRow
import com.gymhelper.app.ui.components.ScreenPadding
import com.gymhelper.app.ui.components.TextInputDialog
import com.gymhelper.app.util.ListCopyFormatter
import com.gymhelper.app.util.ListImportParser
import com.gymhelper.app.weight.WeightSessionViewModel
import com.gymhelper.app.weight.WeightSessionViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun WeightProgramsScreen(
    onBack: () -> Unit,
    onOpenProgram: (Long) -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as GymHelperApp).repository
    val scope = rememberCoroutineScope()
    val programs by repository.observeWeightPrograms().collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    AppScaffold(
        title = "Weight programs",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add program")
            }
        },
    ) { padding ->
        ScreenPadding(padding) {
            if (programs.isEmpty()) {
                Text(
                    "No programs yet. Tap + to create one.",
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(programs, key = { it.id }) { program ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { onOpenProgram(program.id) },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(program.name, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = {
                                    scope.launch { repository.deleteWeightProgram(program.id) }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        TextInputDialog(
            title = "New program",
            confirmLabel = "Create",
            onDismiss = { showAdd = false },
            onConfirm = { name ->
                scope.launch {
                    repository.createWeightProgram(name)
                    showAdd = false
                }
            },
        )
    }
}

@Composable
fun WeightProgramDetailScreen(
    programId: Long,
    onBack: () -> Unit,
    onEditDay: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as GymHelperApp).repository
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val days by repository.observeWeightDays(programId).collectAsState(initial = emptyList())
    var program by remember { mutableStateOf<WeightProgram?>(null) }
    var showAddDay by remember { mutableStateOf(false) }
    var renameProgram by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(programId) {
        program = repository.getWeightProgram(programId)
    }

    AppScaffold(
        title = program?.name ?: "Program",
        onBack = onBack,
        actions = {
            IconButton(onClick = { renameProgram = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Rename")
            }
            IconButton(onClick = { showImport = true }) {
                Icon(Icons.Default.ContentPaste, contentDescription = "Import days")
            }
            IconButton(onClick = { showAddDay = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add day")
            }
        },
    ) { padding ->
        ScreenPadding(padding) {
            ReorderableExerciseList(
                items = days,
                key = { it.id },
                modifier = Modifier.fillMaxSize(),
                onReorder = { ordered ->
                    scope.launch {
                        repository.reorderWeightDays(ordered)
                    }
                },
                itemContent = { day, dragModifier ->
                    WeightDayCard(
                        day = day,
                        modifier = dragModifier,
                        onEdit = { onEditDay(day.id) },
                        onStart = { onStartSession(day.id) },
                        onCopy = {
                            scope.launch {
                                val exercises = repository.getWeightExercises(day.id)
                                val text = ListCopyFormatter.formatWeightDay(day, exercises)
                                clipboard.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            scope.launch { repository.deleteWeightDay(day.id) }
                        },
                    )
                },
            )
        }
    }

    if (showImport) {
        ImportDaysDialog(
            hint = "Paste text copied with the copy-list button. Separate several days with a blank line.",
            onDismiss = { showImport = false },
            onConfirm = { text ->
                val parsed = ListImportParser.parseWeightDays(text)
                scope.launch {
                    repository.importWeightDays(programId, parsed)
                    Toast.makeText(context, "Imported ${parsed.size} day(s)", Toast.LENGTH_SHORT).show()
                    showImport = false
                }
            },
        )
    }

    if (showAddDay) {
        TextInputDialog(
            title = "New exercise day",
            confirmLabel = "Create",
            onDismiss = { showAddDay = false },
            onConfirm = { name ->
                scope.launch {
                    repository.createWeightDay(programId, name)
                    showAddDay = false
                }
            },
        )
    }

    if (renameProgram && program != null) {
        TextInputDialog(
            title = "Rename program",
            confirmLabel = "Save",
            initialValue = program!!.name,
            onDismiss = { renameProgram = false },
            onConfirm = { name ->
                scope.launch {
                    repository.updateWeightProgram(program!!.copy(name = name))
                    program = program!!.copy(name = name)
                    renameProgram = false
                }
            },
        )
    }
}

@Composable
private fun WeightDayCard(
    day: WeightDay,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(day.name, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = onStart) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("Start", modifier = Modifier.padding(start = 4.dp))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy list")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun WeightDayEditScreen(
    dayId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as GymHelperApp).repository
    val scope = rememberCoroutineScope()
    var day by remember { mutableStateOf<WeightDay?>(null) }
    var renameDay by remember { mutableStateOf(false) }
    val exercises by repository.observeWeightExercises(dayId).collectAsState(initial = emptyList())
    var showAddExercise by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<WeightExercise?>(null) }

    androidx.compose.runtime.LaunchedEffect(dayId) {
        day = repository.getWeightDay(dayId)
    }

    AppScaffold(
        title = day?.name ?: "Edit day",
        onBack = onBack,
        actions = {
            IconButton(onClick = { renameDay = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Rename day")
            }
        },
    ) { padding ->
        ScreenPadding(padding) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Exercises", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Long-press and drag to reorder",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { showAddExercise = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add exercise")
                    }
                }
                ReorderableExerciseList(
                    items = exercises,
                    key = { it.id },
                    modifier = Modifier.weight(1f),
                    onReorder = { ordered ->
                        scope.launch {
                            repository.reorderWeightExercises(
                                dayId,
                                ordered.mapIndexed { index, exercise ->
                                    exercise.copy(sortOrder = index)
                                },
                            )
                        }
                    },
                    itemContent = { exercise, dragModifier ->
                        Card(
                            modifier = dragModifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { editingExercise = exercise },
                        ) {
                            ReorderableTextRow(
                                title = exercise.name,
                                subtitle = buildString {
                                    append("${exercise.sets} sets × ${exercise.reps} reps")
                                    exercise.lastWeightKg?.let { append(" · Last: $it kg") }
                                },
                                dragModifier = Modifier,
                                trailing = {
                                    IconButton(onClick = {
                                        scope.launch { repository.deleteWeightExercise(exercise.id) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
    }

    if (renameDay && day != null) {
        TextInputDialog(
            title = "Rename day",
            confirmLabel = "Save",
            initialValue = day!!.name,
            onDismiss = { renameDay = false },
            onConfirm = { name ->
                scope.launch {
                    val updated = day!!.copy(name = name)
                    repository.updateWeightDay(updated)
                    day = updated
                    renameDay = false
                }
            },
        )
    }

    if (showAddExercise) {
        WeightExerciseDialog(
            title = "New exercise",
            onDismiss = { showAddExercise = false },
            onConfirm = { name, sets, reps ->
                scope.launch {
                    repository.addWeightExercise(dayId, name, sets, reps, exercises.size)
                    showAddExercise = false
                }
            },
        )
    }

    editingExercise?.let { exercise ->
        WeightExerciseDialog(
            title = "Edit exercise",
            initialName = exercise.name,
            initialSets = exercise.sets,
            initialReps = exercise.reps,
            onDismiss = { editingExercise = null },
            onConfirm = { name, sets, reps ->
                scope.launch {
                    repository.updateWeightExercise(
                        exercise.copy(name = name, sets = sets, reps = reps),
                    )
                    editingExercise = null
                }
            },
        )
    }
}

@Composable
private fun WeightExerciseDialog(
    title: String,
    initialName: String = "",
    initialSets: Int = 3,
    initialReps: Int = 10,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var sets by remember { mutableStateOf(initialSets.toString()) }
    var reps by remember { mutableStateOf(initialReps.toString()) }

    androidx.compose.material3.AlertDialog(
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
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    val s = sets.toIntOrNull()
                    val r = reps.toIntOrNull()
                    if (name.isNotBlank() && s != null && s > 0 && r != null && r > 0) {
                        onConfirm(name.trim(), s, r)
                    }
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun WeightSessionScreen(
    dayId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: WeightSessionViewModel = viewModel(
        factory = WeightSessionViewModelFactory(context.applicationContext as GymHelperApp, dayId),
    )
    val state by viewModel.state.collectAsState()
    val exercise = state.currentExercise

    AppScaffold(title = state.dayName.ifBlank { "Workout" }, onBack = onBack) { padding ->
        ScreenPadding(padding) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (state.finished) {
                    Text("Workout saved!", style = MaterialTheme.typography.headlineSmall)
                    Button(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 24.dp),
                    ) {
                        Text("Done")
                    }
                } else if (exercise == null) {
                    Text("No exercises in this day.")
                    Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Back")
                    }
                } else {
                    Text(
                        "Set ${state.setNumber} of ${exercise.sets}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        exercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                    Text("Target: ${exercise.reps} reps")
                    OutlinedTextField(
                        value = state.weightInput,
                        onValueChange = viewModel::onWeightChange,
                        label = { Text("Weight (kg)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        singleLine = true,
                    )
                    Button(
                        onClick = { viewModel.completeSet() },
                        enabled = state.weightInput.isNotBlank(),
                    ) {
                        Text("Complete set")
                    }
                }
            }
        }
    }
}
