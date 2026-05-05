package com.todoapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which tab is selected on the list screen */
enum class TaskFilter { ALL, PENDING, COMPLETED }

/** Represents the full UI state for the task list */
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val pendingCount: Int = 0,
    val filter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** One-shot events the UI should consume once */
sealed interface TaskEvent {
    data object TaskAdded : TaskEvent
    data object TaskUpdated : TaskEvent
    data object TaskDeleted : TaskEvent
    data class Error(val message: String) : TaskEvent
}

/**
 * ViewModel for the Task list screen.
 *
 * Exposes [uiState] as a cold [StateFlow] (survives configuration changes) and
 * [events] as a hot [SharedFlow] for side-effects like sounds or snackbars.
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    // ── Filter selection ─────────────────────────────────────────────────

    private val _filter = MutableStateFlow(TaskFilter.ALL)

    // ── Derive the correct task stream from the current filter ───────────

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _tasks: Flow<List<Task>> = _filter.flatMapLatest { filter ->
        when (filter) {
            TaskFilter.ALL       -> repository.getAllTasks()
            TaskFilter.PENDING   -> repository.getPendingTasks()
            TaskFilter.COMPLETED -> repository.getCompletedTasks()
        }
    }

    // ── Pending badge count ──────────────────────────────────────────────

    private val _pendingCount: Flow<Int> = repository.getPendingCount()

    // ── Combine everything into a single UI state ────────────────────────

    val uiState: StateFlow<TaskListUiState> = combine(
        _tasks,
        _pendingCount,
        _filter
    ) { tasks, count, filter ->
        TaskListUiState(
            tasks = tasks,
            pendingCount = count,
            filter = filter,
            isLoading = false
        )
    }.catch { e ->
        emit(TaskListUiState(isLoading = false, errorMessage = e.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListUiState()
    )

    // ── One-shot event bus ───────────────────────────────────────────────

    private val _events = MutableSharedFlow<TaskEvent>()
    val events: SharedFlow<TaskEvent> = _events.asSharedFlow()

    // ── Public actions ───────────────────────────────────────────────────

    /** Change the active filter tab */
    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    /**
     * Add a brand-new task.
     * Emits [TaskEvent.TaskAdded] on success so the UI can play a sound.
     */
    fun addTask(title: String, description: String, priority: Int = 1) {
        if (title.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addTask(
                    Task(
                        title = title.trim(),
                        description = description.trim(),
                        priority = priority
                    )
                )
            }.onSuccess {
                _events.emit(TaskEvent.TaskAdded)
            }.onFailure { e ->
                _events.emit(TaskEvent.Error(e.message ?: "Failed to add task"))
            }
        }
    }

    /**
     * Update an existing task's title, description, or priority.
     */
    fun updateTask(task: Task, newTitle: String, newDescription: String, newPriority: Int) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.updateTask(
                    task.copy(
                        title = newTitle.trim(),
                        description = newDescription.trim(),
                        priority = newPriority,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }.onSuccess {
                _events.emit(TaskEvent.TaskUpdated)
            }.onFailure { e ->
                _events.emit(TaskEvent.Error(e.message ?: "Failed to update task"))
            }
        }
    }

    /**
     * Toggle the isCompleted flag of a task.
     */
    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            runCatching {
                repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            }.onFailure { e ->
                _events.emit(TaskEvent.Error(e.message ?: "Failed to toggle task"))
            }
        }
    }

    /**
     * Delete a single task. Returns the deleted task so the UI can offer undo.
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            runCatching {
                repository.deleteTask(task)
            }.onSuccess {
                _events.emit(TaskEvent.TaskDeleted)
            }.onFailure { e ->
                _events.emit(TaskEvent.Error(e.message ?: "Failed to delete task"))
            }
        }
    }

    /**
     * Undo a deletion by re-inserting the task.
     */
    fun undoDelete(task: Task) {
        viewModelScope.launch {
            runCatching {
                repository.addTask(task)
            }.onFailure { e ->
                _events.emit(TaskEvent.Error(e.message ?: "Failed to restore task"))
            }
        }
    }

    /**
     * Delete all tasks that are marked complete.
     */
    fun deleteAllCompleted() {
        viewModelScope.launch {
            runCatching {
                repository.deleteAllCompleted()
            }.onFailure { e ->
                _events.emit(TaskEvent.Error(e.message ?: "Failed to clear completed"))
            }
        }
    }
}
