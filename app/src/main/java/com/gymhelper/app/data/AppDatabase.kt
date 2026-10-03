package com.gymhelper.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        IntervalProgram::class,
        IntervalDay::class,
        IntervalExercise::class,
        WeightProgram::class,
        WeightDay::class,
        WeightExercise::class,
        WeightSession::class,
        WeightSessionSet::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intervalDao(): IntervalDao
    abstract fun weightDao(): WeightDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE interval_exercises ADD COLUMN durationSeconds INTEGER DEFAULT NULL",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weight_days ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                // Keep the previous alphabetical order for existing days.
                db.execSQL(
                    """
                    UPDATE weight_days SET sortOrder = (
                        SELECT COUNT(*) FROM weight_days d2
                        WHERE d2.programId = weight_days.programId
                        AND (LOWER(d2.name) < LOWER(weight_days.name)
                            OR (LOWER(d2.name) = LOWER(weight_days.name) AND d2.id < weight_days.id))
                    )
                    """.trimIndent(),
                )
                db.execSQL("ALTER TABLE interval_days ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                // Keep the previous alphabetical order for existing days.
                db.execSQL(
                    """
                    UPDATE interval_days SET sortOrder = (
                        SELECT COUNT(*) FROM interval_days d2
                        WHERE d2.programId = interval_days.programId
                        AND (LOWER(d2.name) < LOWER(interval_days.name)
                            OR (LOWER(d2.name) = LOWER(interval_days.name) AND d2.id < interval_days.id))
                    )
                    """.trimIndent(),
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_helper.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { instance = it }
            }
        }
    }
}
