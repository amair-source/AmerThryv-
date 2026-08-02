package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.RoutineDay
import com.example.data.db.RoutineTask
import com.example.ui.theme.*

@Composable
fun RoutinesScreen(
    routineDays: List<RoutineDay>,
    routineTasks: List<RoutineTask>,
    onAddDayClick: () -> Unit,
    onAddTaskClick: (Long) -> Unit,
    onToggleTaskCompleted: (RoutineTask) -> Unit,
    onDeleteDay: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    var selectedDayId by remember(routineDays) {
        mutableStateOf(routineDays.firstOrNull()?.id ?: 0L)
    }

    val currentDay = routineDays.find { it.id == selectedDayId } ?: routineDays.firstOrNull()
    val tasksForCurrentDay = routineTasks.filter { it.dayId == (currentDay?.id ?: 0L) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUSTOM ROUTINES & DAYS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Non-Gym Routine Manager",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onAddDayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_routine_day_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Day", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Routine Day", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Routine Days Tabs ---
        if (routineDays.isNotEmpty()) {
            item {
                ScrollableTabRow(
                    selectedTabIndex = routineDays.indexOfFirst { it.id == (currentDay?.id ?: 0L) }.coerceAtLeast(0),
                    containerColor = DarkSurface,
                    contentColor = MassOrangePrimary,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    routineDays.forEach { day ->
                        val isSelected = day.id == currentDay?.id
                        Tab(
                            selected = isSelected,
                            onClick = { selectedDayId = day.id },
                            text = {
                                Text(
                                    text = day.dayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MassOrangePrimary else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        }

        // --- Current Day Details Card & Tasks ---
        if (currentDay == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No Routines",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No routine days created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Build home calisthenics splits, bulk meal schedules, or morning shake routines!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MassOrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = MassOrangePrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = currentDay.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MassOrangePrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = currentDay.dayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (currentDay.description.isNotBlank()) {
                                    Text(
                                        text = currentDay.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = { onAddTaskClick(currentDay.id) }) {
                                    Icon(Icons.Default.AddTask, contentDescription = "Add Task", tint = MassOrangePrimary)
                                }
                                IconButton(onClick = { onDeleteDay(currentDay.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Day", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Main Tasks & Routine Items (${tasksForCurrentDay.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (tasksForCurrentDay.isEmpty()) {
                item {
                    Text(
                        text = "No tasks added to this day. Tap + to add bodyweight exercises, shakes, or meal tasks!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                items(tasksForCurrentDay, key = { it.id }) { task ->
                    RoutineTaskCard(
                        task = task,
                        onToggleCompleted = { onToggleTaskCompleted(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineTaskCard(
    task: RoutineTask,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val subtasksList = remember(task.subtasksJson) {
        try {
            if (task.subtasksJson.isBlank() || task.subtasksJson == "[]") emptyList()
            else {
                org.json.JSONArray(task.subtasksJson).let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                }
            }
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) DarkSurfaceVariant.copy(alpha = 0.5f) else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isCompleted) SuccessGreen.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleCompleted() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SuccessGreen,
                            uncheckedColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = task.mainTaskName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) TextMuted else TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = when (task.difficultyLevel) {
                                    "Easy" -> SuccessGreen.copy(alpha = 0.15f)
                                    "Hard" -> MassOrangePrimary.copy(alpha = 0.2f)
                                    else -> ProgressBlue.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = task.difficultyLevel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (task.difficultyLevel) {
                                        "Easy" -> SuccessGreen
                                        "Hard" -> MassOrangePrimary
                                        else -> ProgressBlue
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (task.targetSetsReps.isNotBlank()) {
                            Text(
                                text = "Target: ${task.targetSetsReps} (${task.itemType})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = TextMuted
                    )
                }
            }

            if (subtasksList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sub-tasks / Step Guide:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    subtasksList.forEach { subtask ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subtask,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
