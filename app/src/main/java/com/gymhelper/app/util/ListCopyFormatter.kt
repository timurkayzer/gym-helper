package com.gymhelper.app.util

import com.gymhelper.app.data.IntervalDay
import com.gymhelper.app.data.IntervalExercise
import com.gymhelper.app.data.WeightDay
import com.gymhelper.app.data.WeightExercise

object ListCopyFormatter {
    fun formatWeightDay(day: WeightDay, exercises: List<WeightExercise>): String {
        val header = day.name.trim()
        val lines = exercises.map { exercise ->
            "${exercise.name} — ${exercise.sets} sets × ${exercise.reps} reps"
        }
        return buildString {
            appendLine(header)
            lines.forEach { appendLine(it) }
        }.trimEnd()
    }

    fun formatIntervalDay(day: IntervalDay, exercises: List<IntervalExercise>): String {
        val header = buildString {
            append(day.name.trim())
            append(" (")
            append("${day.rounds} rounds, ${day.roundSeconds}s work")
            append(", ${day.restBetweenExercisesSeconds}s between exercises")
            append(", ${day.restBetweenRoundsSeconds}s between rounds")
            append(")")
        }
        val lines = exercises.map { exercise ->
            val seconds = exercise.durationSeconds ?: day.roundSeconds
            if (exercise.durationSeconds != null) {
                "${exercise.name} (${seconds}s)"
            } else {
                exercise.name
            }
        }
        return buildString {
            appendLine(header)
            lines.forEach { appendLine(it) }
        }.trimEnd()
    }
}
