package com.todoapp.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todoapp.domain.model.Task
import com.todoapp.presentation.ui.components.*
import com.todoapp.presentation.viewmodel.*
import kotlinx.coroutines.launch

/**
 * Main task list screen.
 *
 * Features:
 * - Animated header with pending badge
 * - Tab filter row (All / Pending / Completed)
 * - Animated LazyColumn with enter/exit transitions per item
 * - FAB with bounce scale animation
 * - Swipe-to-delete with Snackbar undo
 * - Add / Edit dialogs
 * - Empty state illustration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = hiltViewModel(),
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayAdd: () -> Unit,
    onPlayComplete: () -> Unit,
    onPlayDelete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog state
    var showAddDialog    by remember { mutableStateOf(false) }
    var editingTask      by remember { mutableStateOf<Task?>(null) }
    var taskToDelete     by remember { mutableStateOf<Task?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Consume ViewModel events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskEvent.TaskAdded   -> onPlayAdd()
                is TaskEvent.TaskDeleted -> {
                    onPlayDelete()
                    /* undo offered via snackbar elsewhere */
                }
                is TaskEvent.TaskUpdated -> { /* optional sound */ }
                is TaskEvent.Error       -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedFab(onClick = { showAddDialog = true })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Header ──────────────────────────────────────────────────
            TaskListHeader(
                pendingCount = uiState.pendingCount,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onClearCompleted = { showClearConfirm = true }
            )

            // ── Filter tabs ─────────────────────────────────────────────
            FilterTabRow(
                selectedFilter = uiState.filter,
                onFilterChange = { viewModel.setFilter(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Content ─────────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.tasks.isEmpty()) {
                EmptyState(filter = uiState.filter.name)
            } else {
                AnimatedTaskList(
                    tasks = uiState.tasks,
                    onToggle = { task ->
                        viewModel.toggleComplete(task)
                        onPlayComplete()
                    },
                    onDelete = { task ->
                        taskToDelete = task
                    },
                    onEdit = { task -> editingTask = task }
                )
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        TaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, prio ->
                viewModel.addTask(title, desc, prio)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    editingTask?.let { task ->
        TaskDialog(
            existingTask = task,
            onDismiss = { editingTask = null },
            onConfirm = { title, desc, prio ->
                viewModel.updateTask(task, title, desc, prio)
                editingTask = null
            }
        )
    }

    // Delete Single Task Confirmation
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    taskToDelete = null
                    // Snackbar with undo
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${task.title}\" deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDelete(task)
                        }
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Completely") },
            text = { Text("Are you sure you want to remove all completed tasks from the dashboard?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllCompleted()
                    showClearConfirm = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TaskListHeader(
    pendingCount: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onClearCompleted: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            AnimatedContent(
                targetState = pendingCount,
                transitionSpec = {
                    slideInVertically { -it } + fadeIn() togetherWith
                    slideOutVertically { it } + fadeOut()
                },
                label = "pendingCount"
            ) { count ->
                Text(
                    text = when (count) {
                        0    -> "All caught up! ✅"
                        1    -> "1 task pending"
                        else -> "$count tasks pending"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Theme toggle button
        IconButton(onClick = onToggleTheme) {
            Crossfade(
                targetState = isDarkTheme,
                animationSpec = tween(300),
                label = "themeIcon"
            ) { dark ->
                Icon(
                    imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (dark) "Switch to light mode" else "Switch to dark mode",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Clear completely") },
                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                    onClick = {
                        onClearCompleted()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterTabRow(
    selectedFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    val filters = listOf(TaskFilter.ALL, TaskFilter.PENDING, TaskFilter.COMPLETED)
    val labels  = listOf("All", "Pending", "Done")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        filters.forEachIndexed { index, filter ->
            val selected = selectedFilter == filter
            val weight   by animateFloatAsState(
                targetValue = if (selected) 1.05f else 1f,
                label = "tabWeight"
            )

            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(44.dp)
                    .scale(weight)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected)
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        else Brush.horizontalGradient(listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        ))
                    ),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { onFilterChange(filter) }) {
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedTaskList(
    tasks: List<Task>,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(
            items = tasks,
            key = { it.id }
        ) { task ->
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(250)
                ) + fadeOut(tween(200))
            ) {
                TaskCard(
                    task = task,
                    onToggle = onToggle,
                    onDelete = onDelete,
                    onEdit = onEdit
                )
            }
        }
    }
}

@Composable
private fun AnimatedFab(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "fabScale"
    )

    FloatingActionButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier.scale(scale),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor   = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add task", modifier = Modifier.size(28.dp))
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(150)
            pressed = false
        }
    }
}
