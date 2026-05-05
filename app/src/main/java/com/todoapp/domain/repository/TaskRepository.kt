package com.todoapp.domain.repository

import com.todoapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Contract that the data layer must fulfil.
 * This abstraction makes the ViewModel testable without a real database.
 */
interface TaskRepository {

    /** Stream of all tasks ordered by creation time descending. */
    fun getAllTasks(): Flow<List<Task>>

    /** Stream of pending (incomplete) tasks. */
    fun getPendingTasks(): Flow<List<Task>>

    /** Stream of completed tasks. */
    fun getCompletedTasks(): Flow<List<Task>>

    /** Stream of pending task count — useful for badges. */
    fun getPendingCount(): Flow<Int>

    /** Fetch one task by id; null if missing. */
    suspend fun getTaskById(id: Long): Task?

    /** Persist a new task; returns its generated id. */
    suspend fun addTask(task: Task): Long

    /** Overwrite an existing task record. */
    suspend fun updateTask(task: Task)

    /** Remove a task permanently. */
    suspend fun deleteTask(task: Task)

    /** Bulk-delete every completed task. */
    suspend fun deleteAllCompleted()
}
