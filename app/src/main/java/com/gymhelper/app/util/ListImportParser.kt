package com.gymhelper.app.util

data class ParsedWeightExercise(val name: String, val sets: Int, val reps: Int)

data class ParsedWeightDay(val name: String, val exercises: List<ParsedWeightExercise>)

data class ParsedIntervalExercise(val name: String, val durationSeconds: Int?)

data class ParsedIntervalDay(
    val name: String,
    val rounds: Int?,
    val roundSeconds: Int?,
    val restBetweenExercisesSeconds: Int?,
    val restBetweenRoundsSeconds: Int?,
    val exercises: List<ParsedIntervalExercise>,
)

/** Parses text produced by [ListCopyFormatter]. Several days may be separated by blank lines. */
object ListImportParser {
    private val weightLine = Regex(
        """^(.+?)\s*[—–-]\s*(\d+)\s*sets?\s*[×xX*]\s*(\d+)\s*reps?\s*$""",
        RegexOption.IGNORE_CASE,
    )
    private val intervalHeader = Regex("""^(.+?)\s*\((.*)\)\s*$""")
    private val intervalExercise = Regex("""^(.+?)\s*\((\d+)\s*s\)\s*$""", RegexOption.IGNORE_CASE)
    private val rounds = Regex("""(\d+)\s*rounds?""", RegexOption.IGNORE_CASE)
    private val work = Regex("""(\d+)\s*s\s*work""", RegexOption.IGNORE_CASE)
    private val betweenExercises = Regex("""(\d+)\s*s\s*between exercises""", RegexOption.IGNORE_CASE)
    private val betweenRounds = Regex("""(\d+)\s*s\s*between rounds""", RegexOption.IGNORE_CASE)

    private fun blocks(text: String): List<List<String>> =
        text.lines().map { it.trim() }
            .fold(mutableListOf(mutableListOf<String>())) { acc, line ->
                if (line.isEmpty()) {
                    if (acc.last().isNotEmpty()) acc.add(mutableListOf())
                } else {
                    acc.last().add(line)
                }
                acc
            }
            .filter { it.isNotEmpty() }

    fun parseWeightDays(text: String): List<ParsedWeightDay> = blocks(text).map { lines ->
        ParsedWeightDay(
            name = lines.first(),
            exercises = lines.drop(1).map { line ->
                weightLine.matchEntire(line)?.let {
                    ParsedWeightExercise(
                        it.groupValues[1].trim(),
                        it.groupValues[2].toInt().coerceAtLeast(1),
                        it.groupValues[3].toInt().coerceAtLeast(1),
                    )
                } ?: ParsedWeightExercise(line, sets = 3, reps = 10)
            },
        )
    }

    fun parseIntervalDays(text: String): List<ParsedIntervalDay> = blocks(text).map { lines ->
        val header = lines.first()
        val match = intervalHeader.matchEntire(header)
        val options = match?.groupValues?.get(2).orEmpty()
        fun find(regex: Regex) = regex.find(options)?.groupValues?.get(1)?.toInt()
        ParsedIntervalDay(
            name = match?.groupValues?.get(1)?.trim() ?: header,
            rounds = find(rounds)?.coerceAtLeast(1),
            roundSeconds = find(work)?.coerceAtLeast(1),
            restBetweenExercisesSeconds = find(betweenExercises),
            restBetweenRoundsSeconds = find(betweenRounds),
            exercises = lines.drop(1).map { line ->
                intervalExercise.matchEntire(line)?.let {
                    ParsedIntervalExercise(it.groupValues[1].trim(), it.groupValues[2].toInt())
                } ?: ParsedIntervalExercise(line, null)
            },
        )
    }
}
