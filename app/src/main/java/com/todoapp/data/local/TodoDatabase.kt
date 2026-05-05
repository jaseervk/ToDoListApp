package com.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The Room database for the Todo application.
 *
 * Increment [version] whenever the schema changes and provide a migration or
 * set `fallbackToDestructiveMigration()` in the builder (acceptable for development).
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "todo_database"
    }
}
