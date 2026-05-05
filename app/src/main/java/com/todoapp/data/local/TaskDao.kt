package com.todoapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [TaskEntity].
 * All write operations use suspend functions for coroutine safety.
 * Read operations expose a [Flow] so the UI reacts to DB changes automatically.
 */
@Dao
interface TaskDao {

    /** Observe all tasks ordered by timestamp descending (newest first). */
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasksStream(): Flow<List<TaskEntity>>

    /** Observe only incomplete tasks. */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY timestamp DESC")
    fun getPendingTasksStream(): Flow<List<TaskEntity>>

    /** Observe only completed tasks. */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getCompletedTasksStream(): Flow<List<TaskEntity>>

    /** Fetch a single task by its ID; null if not found. */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    /** Insert or replace a task. Returns the new row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    /** Update an existing task record. */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /** Delete a specific task record. */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /** Delete all completed tasks at once. */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteAllCompleted()

    /** Count pending tasks (used for badge/notification). */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingCountStream(): Flow<Int>
}
