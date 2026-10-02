package com.gymhelper.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_programs ORDER BY name COLLATE NOCASE")
    fun observePrograms(): Flow<List<WeightProgram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: WeightProgram): Long

    @Update
    suspend fun updateProgram(program: WeightProgram)

    @Query("DELETE FROM weight_programs WHERE id = :programId")
    suspend fun deleteProgram(programId: Long)

    @Query("SELECT * FROM weight_programs WHERE id = :programId")
    suspend fun getProgram(programId: Long): WeightProgram?

    @Query("SELECT * FROM weight_days WHERE programId = :programId ORDER BY name COLLATE NOCASE")
    fun observeDays(programId: Long): Flow<List<WeightDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: WeightDay): Long

    @Update
    suspend fun updateDay(day: WeightDay)

    @Query("DELETE FROM weight_days WHERE id = :dayId")
    suspend fun deleteDay(dayId: Long)

    @Query("SELECT * FROM weight_days WHERE id = :dayId")
    suspend fun getDay(dayId: Long): WeightDay?

    @Query("SELECT * FROM weight_exercises WHERE dayId = :dayId ORDER BY sortOrder")
    fun observeExercises(dayId: Long): Flow<List<WeightExercise>>

    @Query("SELECT * FROM weight_exercises WHERE dayId = :dayId ORDER BY sortOrder")
    suspend fun getExercises(dayId: Long): List<WeightExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: WeightExercise): Long

    @Update
    suspend fun updateExercise(exercise: WeightExercise)

    @Query("DELETE FROM weight_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    @Transaction
    suspend fun reorderExercises(dayId: Long, ordered: List<WeightExercise>) {
        ordered.forEachIndexed { index, exercise ->
            updateExercise(exercise.copy(sortOrder = index))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WeightSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionSets(sets: List<WeightSessionSet>)

    @Query(
        """
        SELECT weightKg FROM weight_session_sets
        WHERE exerciseId = :exerciseId
        ORDER BY sessionId DESC, setNumber DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestWeightForExercise(exerciseId: Long): Double?

    @Transaction
    suspend fun saveSession(
        dayId: Long,
        completedAtEpochMs: Long,
        sets: List<WeightSessionSet>,
        exerciseLastWeights: Map<Long, Double>,
    ) {
        val sessionId = insertSession(
            WeightSession(dayId = dayId, completedAtEpochMs = completedAtEpochMs),
        )
        insertSessionSets(sets.map { it.copy(sessionId = sessionId) })
        exerciseLastWeights.forEach { (exerciseId, weight) ->
            val exercise = getExercise(exerciseId)
            if (exercise != null) {
                updateExercise(exercise.copy(lastWeightKg = weight))
            }
        }
    }

    @Query("SELECT * FROM weight_exercises WHERE id = :exerciseId")
    suspend fun getExercise(exerciseId: Long): WeightExercise?
}
