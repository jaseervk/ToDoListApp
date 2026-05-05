package com.todoapp.domain.model

/**
 * Domain model for a Task — decoupled from the Room entity.
 * The repository layer maps between [Task] and [TaskEntity].
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    /** 0 = Low, 1 = Medium, 2 = High */
    val priority: Int = 1
)
