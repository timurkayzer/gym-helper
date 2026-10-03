package com.gymhelper.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "interval_programs")
data class IntervalProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "interval_days",
    foreignKeys = [
        ForeignKey(
            entity = IntervalProgram::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("programId")],
)
data class IntervalDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val name: String,
    val roundSeconds: Int = 30,
    val restBetweenExercisesSeconds: Int = 10,
    val restBetweenRoundsSeconds: Int = 60,
    val rounds: Int = 3,
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
)

@Entity(
    tableName = "interval_exercises",
    foreignKeys = [
        ForeignKey(
            entity = IntervalDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dayId")],
)
data class IntervalExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val name: String,
    val sortOrder: Int,
    /** When null, the day's default round duration is used. */
    val durationSeconds: Int? = null,
)
