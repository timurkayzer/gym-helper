package com.gymhelper.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_programs")
data class WeightProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "weight_days",
    foreignKeys = [
        ForeignKey(
            entity = WeightProgram::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("programId")],
)
data class WeightDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val name: String,
)

@Entity(
    tableName = "weight_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WeightDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dayId")],
)
data class WeightExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    val sortOrder: Int,
    val lastWeightKg: Double? = null,
)

@Entity(
    tableName = "weight_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WeightDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dayId")],
)
data class WeightSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val completedAtEpochMs: Long,
)

@Entity(
    tableName = "weight_session_sets",
    foreignKeys = [
        ForeignKey(
            entity = WeightSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WeightExercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("exerciseId")],
)
data class WeightSessionSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val weightKg: Double,
)
