package com.gymhelper.app.interval

enum class IntervalPhaseKind {
    EXERCISE,
    REST_BETWEEN_EXERCISES,
    REST_BETWEEN_ROUNDS,
    FINISHED,
}

data class IntervalPhase(
    val kind: IntervalPhaseKind,
    val label: String,
    val durationSeconds: Int,
)

data class IntervalSessionState(
    val phases: List<IntervalPhase> = emptyList(),
    val currentPhaseIndex: Int = 0,
    val secondsRemaining: Int = 0,
    val isPaused: Boolean = false,
    val isRunning: Boolean = false,
    val sessionStarted: Boolean = false,
) {
    val currentPhase: IntervalPhase? = phases.getOrNull(currentPhaseIndex)
    val isFinished: Boolean = currentPhase?.kind == IntervalPhaseKind.FINISHED

    val canSkipPhase: Boolean
        get() = sessionStarted && !isFinished && when (currentPhase?.kind) {
            IntervalPhaseKind.EXERCISE,
            IntervalPhaseKind.REST_BETWEEN_EXERCISES,
            IntervalPhaseKind.REST_BETWEEN_ROUNDS,
            -> true
            else -> false
        }

    val blockNavigationBack: Boolean
        get() = sessionStarted && !isFinished
}

data class IntervalExercisePhaseInput(
    val name: String,
    val durationSeconds: Int,
)

fun buildIntervalPhases(
    exercises: List<IntervalExercisePhaseInput>,
    restBetweenExercisesSeconds: Int,
    restBetweenRoundsSeconds: Int,
    rounds: Int,
): List<IntervalPhase> {
    if (exercises.isEmpty()) {
        return listOf(IntervalPhase(IntervalPhaseKind.FINISHED, "No exercises", 0))
    }

    val phases = mutableListOf<IntervalPhase>()
    for (round in 1..rounds) {
        exercises.forEachIndexed { index, exercise ->
            phases += IntervalPhase(
                kind = IntervalPhaseKind.EXERCISE,
                label = exercise.name,
                durationSeconds = exercise.durationSeconds,
            )
            val isLastExercise = index == exercises.lastIndex
            val isLastRound = round == rounds
            if (!isLastExercise) {
                phases += IntervalPhase(
                    kind = IntervalPhaseKind.REST_BETWEEN_EXERCISES,
                    label = "Rest",
                    durationSeconds = restBetweenExercisesSeconds,
                )
            } else if (!isLastRound) {
                phases += IntervalPhase(
                    kind = IntervalPhaseKind.REST_BETWEEN_ROUNDS,
                    label = "Round rest",
                    durationSeconds = restBetweenRoundsSeconds,
                )
            }
        }
    }
    phases += IntervalPhase(IntervalPhaseKind.FINISHED, "Done", 0)
    return phases
}
