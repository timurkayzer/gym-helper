package com.gymhelper.app.data

import kotlinx.coroutines.flow.Flow

class GymRepository(
    private val intervalDao: IntervalDao,
    private val weightDao: WeightDao,
) {
    fun observeIntervalPrograms(): Flow<List<IntervalProgram>> = intervalDao.observePrograms()

    suspend fun createIntervalProgram(name: String): Long =
        intervalDao.insertProgram(IntervalProgram(name = name))

    suspend fun updateIntervalProgram(program: IntervalProgram) = intervalDao.updateProgram(program)

    suspend fun deleteIntervalProgram(programId: Long) = intervalDao.deleteProgram(programId)

    suspend fun getIntervalProgram(programId: Long) = intervalDao.getProgram(programId)

    fun observeIntervalDays(programId: Long) = intervalDao.observeDays(programId)

    suspend fun createIntervalDay(programId: Long, name: String): Long =
        intervalDao.insertDay(IntervalDay(programId = programId, name = name))

    suspend fun updateIntervalDay(day: IntervalDay) = intervalDao.updateDay(day)

    suspend fun deleteIntervalDay(dayId: Long) = intervalDao.deleteDay(dayId)

    suspend fun getIntervalDay(dayId: Long) = intervalDao.getDay(dayId)

    fun observeIntervalExercises(dayId: Long) = intervalDao.observeExercises(dayId)

    suspend fun getIntervalExercises(dayId: Long) = intervalDao.getExercises(dayId)

    suspend fun addIntervalExercise(
        dayId: Long,
        name: String,
        sortOrder: Int,
        durationSeconds: Int? = null,
    ): Long = intervalDao.insertExercise(
        IntervalExercise(
            dayId = dayId,
            name = name,
            sortOrder = sortOrder,
            durationSeconds = durationSeconds,
        ),
    )

    suspend fun reorderIntervalExercises(dayId: Long, ordered: List<IntervalExercise>) =
        intervalDao.reorderExercises(dayId, ordered)

    suspend fun updateIntervalExercise(exercise: IntervalExercise) =
        intervalDao.updateExercise(exercise)

    suspend fun deleteIntervalExercise(exerciseId: Long) = intervalDao.deleteExercise(exerciseId)

    fun observeWeightPrograms(): Flow<List<WeightProgram>> = weightDao.observePrograms()

    suspend fun createWeightProgram(name: String): Long =
        weightDao.insertProgram(WeightProgram(name = name))

    suspend fun updateWeightProgram(program: WeightProgram) = weightDao.updateProgram(program)

    suspend fun deleteWeightProgram(programId: Long) = weightDao.deleteProgram(programId)

    suspend fun getWeightProgram(programId: Long) = weightDao.getProgram(programId)

    fun observeWeightDays(programId: Long) = weightDao.observeDays(programId)

    suspend fun createWeightDay(programId: Long, name: String): Long =
        weightDao.insertDay(WeightDay(programId = programId, name = name))

    suspend fun updateWeightDay(day: WeightDay) = weightDao.updateDay(day)

    suspend fun deleteWeightDay(dayId: Long) = weightDao.deleteDay(dayId)

    suspend fun getWeightDay(dayId: Long) = weightDao.getDay(dayId)

    fun observeWeightExercises(dayId: Long) = weightDao.observeExercises(dayId)

    suspend fun getWeightExercises(dayId: Long) = weightDao.getExercises(dayId)

    suspend fun addWeightExercise(
        dayId: Long,
        name: String,
        sets: Int,
        reps: Int,
        sortOrder: Int,
    ): Long = weightDao.insertExercise(
        WeightExercise(dayId = dayId, name = name, sets = sets, reps = reps, sortOrder = sortOrder),
    )

    suspend fun updateWeightExercise(exercise: WeightExercise) = weightDao.updateExercise(exercise)

    suspend fun deleteWeightExercise(exerciseId: Long) = weightDao.deleteExercise(exerciseId)

    suspend fun reorderWeightExercises(dayId: Long, ordered: List<WeightExercise>) =
        weightDao.reorderExercises(dayId, ordered)

    suspend fun saveWeightSession(
        dayId: Long,
        completedAtEpochMs: Long,
        sets: List<WeightSessionSet>,
        exerciseLastWeights: Map<Long, Double>,
    ) = weightDao.saveSession(dayId, completedAtEpochMs, sets, exerciseLastWeights)
}
