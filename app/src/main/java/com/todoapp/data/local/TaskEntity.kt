package com.todoapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a single Todo task.
 * Maps to the "tasks" table in the local database.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Short title of the task */
    val title: String,

    /** Optional longer description */
    val description: String = "",

    /** Creation / last-modified timestamp in millis */
    val timestamp: Long = System.currentTimeMillis(),

    /** Whether the task has been completed */
    val isCompleted: Boolean = false,

    /** Priority level: 0 = Low, 1 = Medium, 2 = High */
    val priority: Int = 1
)
