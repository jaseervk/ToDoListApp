package com.todoapp.data.repository

import com.todoapp.data.local.TaskDao
import com.todoapp.data.local.TaskEntity
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [TaskRepository] that reads/writes via Room.
 * Mapper functions convert between [TaskEntity] (data layer) and [Task] (domain layer).
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private fun TaskEntity.toDomain() = Task(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp,
        isCompleted = isCompleted,
        priority = priority
    )

    private fun Task.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp,
        isCompleted = isCompleted,
        priority = priority
    )

    // ── Repository implementation ─────────────────────────────────────────

    override fun getAllTasks(): Flow<List<Task>> =
        dao.getAllTasksStream().map { list -> list.map { it.toDomain() } }

    override fun getPendingTasks(): Flow<List<Task>> =
        dao.getPendingTasksStream().map { list -> list.map { it.toDomain() } }

    override fun getCompletedTasks(): Flow<List<Task>> =
        dao.getCompletedTasksStream().map { list -> list.map { it.toDomain() } }

    override fun getPendingCount(): Flow<Int> =
        dao.getPendingCountStream()

    override suspend fun getTaskById(id: Long): Task? =
        dao.getTaskById(id)?.toDomain()

    override suspend fun addTask(task: Task): Long =
        dao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) =
        dao.updateTask(task.toEntity())

    override suspend fun deleteTask(task: Task) =
        dao.deleteTask(task.toEntity())

    override suspend fun deleteAllCompleted() =
        dao.deleteAllCompleted()
}
