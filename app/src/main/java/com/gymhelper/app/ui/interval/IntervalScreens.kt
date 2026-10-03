package com.gymhelper.app.ui.interval

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymhelper.app.GymHelperApp
import com.gymhelper.app.data.IntervalDay
import com.gymhelper.app.data.IntervalExercise
import com.gymhelper.app.data.IntervalProgram
import com.gymhelper.app.interval.IntervalPhaseKind
import com.gymhelper.app.interval.IntervalSessionViewModel
import com.gymhelper.app.interval.IntervalSessionViewModelFactory
import com.gymhelper.app.ui.components.AppScaffold
import com.gymhelper.app.ui.components.HoldToStopButton
import com.gymhelper.app.ui.components.IntervalExerciseDialog
import com.gymhelper.app.ui.components.KeepScreenOnEffect
import com.gymhelper.app.ui.components.SessionActionButton
import com.gymhelper.app.ui.components.NumberFieldSpec
import com.gymhelper.app.ui.components.NumberInputDialog
import com.gymhelper.app.ui.components.ImportDaysDialog
import com.gymhelper.app.ui.components.ReorderableExerciseList
import com.gymhelper.app.ui.components.ReorderableTextRow
import com.gymhelper.app.ui.components.ScreenPadding
import com.gymhelper.app.ui.components.TextInputDialog
import com.gymhelper.app.ui.theme.SessionContinueGreen
import com.gymhelper.app.ui.theme.SessionPauseYellow
import com.gymhelper.app.util.ListCopyFormatter
import com.gymhelper.app.util.ListImportParser
import com.gymhelper.app.util.formatSecondsAsMmSs
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun IntervalProgramsScreen(
    onBack: () -> Unit,
    onOpenProgram: (Long) -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as GymHelperApp).repository
    val scope = rememberCoroutineScope()
    val programs by repository.observeIntervalPrograms().collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    AppScaffold(
        title = "Interval programs",
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
                                    scope.launch { repository.deleteIntervalProgram(program.id) }
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
                    repository.createIntervalProgram(name)
                    showAdd = false
                }
            },
        )
    }
}

@Composable
fun IntervalProgramDetailScreen(
    programId: Long,
    onBack: () -> Unit,
    onEditDay: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as GymHelperApp).repository
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val days by repository.observeIntervalDays(programId).collectAsState(initial = emptyList())
    var program by remember { mutableStateOf<IntervalProgram?>(null) }
    var showAddDay by remember { mutableStateOf(false) }
    var renameProgram by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(programId) {
        program = repository.getIntervalProgram(programId)
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
                        repository.reorderIntervalDays(ordered)
                    }
                },
                itemContent = { day, dragModifier ->
                    IntervalDayCard(
                        day = day,
                        modifier = dragModifier,
                        onEdit = { onEditDay(day.id) },
                        onStart = { onStartSession(day.id) },
                        onCopy = {
                            scope.launch {
                                val exercises = repository.getIntervalExercises(day.id)
                                val text = ListCopyFormatter.formatIntervalDay(day, exercises)
                                clipboard.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            scope.launch { repository.deleteIntervalDay(day.id) }
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
                val parsed = ListImportParser.parseIntervalDays(text)
                scope.launch {
                    repository.importIntervalDays(programId, parsed)
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
                    repository.createIntervalDay(programId, name)
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
                    repository.updateIntervalProgram(program!!.copy(name = name))
                    program = program!!.copy(name = name)
                    renameProgram = false
                }
            },
        )
    }
}

@Composable
private fun IntervalDayCard(
    day: IntervalDay,
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
            Text(
                "${day.rounds} rounds · ${day.roundSeconds}s work · ${day.restBetweenExercisesSeconds}s rest · ${day.restBetweenRoundsSeconds}s round rest",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
fun IntervalDayEditScreen(
    dayId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as GymHelperApp).repository
    val scope = rememberCoroutineScope()
    var day by remember { mutableStateOf<IntervalDay?>(null) }
    var renameDay by remember { mutableStateOf(false) }
    val exercises by repository.observeIntervalExercises(dayId).collectAsState(initial = emptyList())
    var showAddExercise by remember { mutableStateOf(false) }
    var showTiming by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<IntervalExercise?>(null) }

    androidx.compose.runtime.LaunchedEffect(dayId) {
        day = repository.getIntervalDay(dayId)
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
                day?.let { current ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { showTiming = true },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Timer settings", fontWeight = FontWeight.SemiBold)
                            Text("Default round: ${current.roundSeconds}s")
                            Text("Rest between exercises: ${current.restBetweenExercisesSeconds}s")
                            Text("Rest between rounds: ${current.restBetweenRoundsSeconds}s")
                            Text("Rounds: ${current.rounds}")
                            Text("Tap to edit", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
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
                            repository.reorderIntervalExercises(
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
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { editingExercise = exercise },
                        ) {
                            ReorderableTextRow(
                                title = exercise.name,
                                subtitle = exercise.durationSeconds?.let { custom ->
                                    "Custom: ${formatSecondsAsMmSs(custom)}"
                                } ?: day?.let { "Default: ${formatSecondsAsMmSs(it.roundSeconds)}" },
                                dragModifier = Modifier,
                                trailing = {
                                    IconButton(onClick = {
                                        scope.launch { repository.deleteIntervalExercise(exercise.id) }
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

    val defaultRound = day?.roundSeconds ?: 30
    if (renameDay && day != null) {
        TextInputDialog(
            title = "Rename day",
            confirmLabel = "Save",
            initialValue = day!!.name,
            onDismiss = { renameDay = false },
            onConfirm = { name ->
                scope.launch {
                    val updated = day!!.copy(name = name)
                    repository.updateIntervalDay(updated)
                    day = updated
                    renameDay = false
                }
            },
        )
    }

    if (showAddExercise) {
        IntervalExerciseDialog(
            title = "New exercise",
            defaultRoundSeconds = defaultRound,
            onDismiss = { showAddExercise = false },
            onConfirm = { name, duration ->
                scope.launch {
                    repository.addIntervalExercise(dayId, name, exercises.size, duration)
                    showAddExercise = false
                }
            },
        )
    }

    editingExercise?.let { exercise ->
        IntervalExerciseDialog(
            title = "Edit exercise",
            defaultRoundSeconds = defaultRound,
            initialName = exercise.name,
            initialCustomDuration = exercise.durationSeconds,
            onDismiss = { editingExercise = null },
            onConfirm = { name, duration ->
                scope.launch {
                    repository.updateIntervalExercise(
                        exercise.copy(name = name, durationSeconds = duration),
                    )
                    editingExercise = null
                }
            },
        )
    }

    if (showTiming && day != null) {
        NumberInputDialog(
            title = "Timer settings",
            fields = listOf(
                NumberFieldSpec("round", "Default round (seconds)", day!!.roundSeconds, min = 1),
                NumberFieldSpec("restEx", "Rest between exercises (seconds)", day!!.restBetweenExercisesSeconds, min = 0),
                NumberFieldSpec("restRound", "Rest between rounds (seconds)", day!!.restBetweenRoundsSeconds, min = 0),
                NumberFieldSpec("rounds", "Rounds", day!!.rounds, min = 1),
            ),
            onDismiss = { showTiming = false },
            onConfirm = { values ->
                scope.launch {
                    val updated = day!!.copy(
                        roundSeconds = values["round"]!!,
                        restBetweenExercisesSeconds = values["restEx"]!!,
                        restBetweenRoundsSeconds = values["restRound"]!!,
                        rounds = values["rounds"]!!,
                    )
                    repository.updateIntervalDay(updated)
                    day = updated
                    showTiming = false
                }
            },
        )
    }
}

@Composable
fun IntervalSessionScreen(
    dayId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: IntervalSessionViewModel = viewModel(
        factory = IntervalSessionViewModelFactory(context.applicationContext as GymHelperApp, dayId),
    )
    val state by viewModel.state.collectAsState()
    val phase = state.currentPhase

    KeepScreenOnEffect()

    val leaveSession = {
        viewModel.abandon()
        onBack()
    }

    BackHandler(enabled = state.blockNavigationBack) {
        // Ignore system back while training is in progress.
    }
    BackHandler(enabled = !state.blockNavigationBack) {
        leaveSession()
    }

    AppScaffold(
        title = "Interval session",
        backEnabled = !state.blockNavigationBack,
        onBack = leaveSession,
    ) { padding ->
        ScreenPadding(padding) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val subtitle = when (phase?.kind) {
                    IntervalPhaseKind.EXERCISE -> "Exercise"
                    IntervalPhaseKind.REST_BETWEEN_EXERCISES -> "Rest"
                    IntervalPhaseKind.REST_BETWEEN_ROUNDS -> "Round rest"
                    IntervalPhaseKind.FINISHED -> "Finished"
                    null -> ""
                }
                Text(subtitle, style = MaterialTheme.typography.titleLarge)
                Text(
                    phase?.label ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                if (!state.isFinished) {
                    Text(
                        text = formatSecondsAsMmSs(state.secondsRemaining),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth()
                        .widthIn(max = 360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!state.sessionStarted && !state.isFinished) {
                        SessionActionButton(
                            text = "Start",
                            containerColor = SessionContinueGreen,
                            onClick = { viewModel.start() },
                        )
                    }
                    if (state.canSkipPhase) {
                        SessionActionButton(
                            text = "Continue",
                            containerColor = SessionContinueGreen,
                            onClick = { viewModel.skipToNextPhase() },
                        )
                    }
                    if (state.isRunning && !state.isPaused) {
                        SessionActionButton(
                            text = "Pause",
                            containerColor = SessionPauseYellow,
                            contentColor = Color.Black,
                            onClick = { viewModel.pause() },
                        )
                    }
                    if (state.isPaused && !state.isFinished) {
                        SessionActionButton(
                            text = "Resume",
                            containerColor = SessionPauseYellow,
                            contentColor = Color.Black,
                            onClick = { viewModel.resume() },
                        )
                    }
                    if (state.sessionStarted && !state.isFinished) {
                        HoldToStopButton(
                            onStop = {
                                viewModel.stop()
                                onBack()
                            },
                        )
                    }
                    if (state.isFinished) {
                        SessionActionButton(
                            text = "Done",
                            containerColor = SessionContinueGreen,
                            onClick = onBack,
                        )
                    }
                }
            }
        }
    }
}
