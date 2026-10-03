package com.gymhelper.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IntervalDao {
    @Query("SELECT * FROM interval_programs ORDER BY name COLLATE NOCASE")
    fun observePrograms(): Flow<List<IntervalProgram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: IntervalProgram): Long

    @Update
    suspend fun updateProgram(program: IntervalProgram)

    @Query("DELETE FROM interval_programs WHERE id = :programId")
    suspend fun deleteProgram(programId: Long)

    @Query("SELECT * FROM interval_programs WHERE id = :programId")
    suspend fun getProgram(programId: Long): IntervalProgram?

    @Query("SELECT * FROM interval_days WHERE programId = :programId ORDER BY sortOrder, id")
    fun observeDays(programId: Long): Flow<List<IntervalDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: IntervalDay): Long

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM interval_days WHERE programId = :programId")
    suspend fun nextDaySortOrder(programId: Long): Int

    @Transaction
    suspend fun reorderDays(ordered: List<IntervalDay>) {
        ordered.forEachIndexed { index, day ->
            updateDay(day.copy(sortOrder = index))
        }
    }

    @Update
    suspend fun updateDay(day: IntervalDay)

    @Query("DELETE FROM interval_days WHERE id = :dayId")
    suspend fun deleteDay(dayId: Long)

    @Query("SELECT * FROM interval_days WHERE id = :dayId")
    suspend fun getDay(dayId: Long): IntervalDay?

    @Query("SELECT * FROM interval_exercises WHERE dayId = :dayId ORDER BY sortOrder")
    fun observeExercises(dayId: Long): Flow<List<IntervalExercise>>

    @Query("SELECT * FROM interval_exercises WHERE dayId = :dayId ORDER BY sortOrder")
    suspend fun getExercises(dayId: Long): List<IntervalExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: IntervalExercise): Long

    @Update
    suspend fun updateExercise(exercise: IntervalExercise)

    @Query("DELETE FROM interval_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    @Transaction
    suspend fun reorderExercises(dayId: Long, ordered: List<IntervalExercise>) {
        ordered.forEachIndexed { index, exercise ->
            updateExercise(exercise.copy(sortOrder = index))
        }
    }
}
