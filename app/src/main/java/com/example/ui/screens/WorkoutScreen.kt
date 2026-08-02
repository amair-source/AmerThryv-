package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.Exercise
import com.example.data.db.WorkoutLog
import com.example.ui.theme.*

@Composable
fun WorkoutScreen(
    exercises: List<Exercise>,
    todayWorkoutLogs: List<WorkoutLog>,
    allWorkoutLogs: List<WorkoutLog> = emptyList(),
    onLogSet: (Long, String, Double, Int, String) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onAddExerciseClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    var inputWeight by remember { mutableStateOf("") }
    var inputReps by remember { mutableStateOf("") }
    var inputDifficulty by remember { mutableStateOf("Medium") }

    val categories = listOf("All", "Chest", "Legs", "Back", "Shoulders", "Arms", "Core")

    val filteredExercises = remember(selectedCategory, exercises) {
        if (selectedCategory == "All") exercises
        else exercises.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LaunchedEffect(filteredExercises) {
        if (selectedExercise == null && filteredExercises.isNotEmpty()) {
            selectedExercise = filteredExercises.first()
        }
    }

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
                        text = "BODYWEIGHT & HOME OVERLOAD",
                        style = MaterialTheme.typography.labelMedium,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calisthenics Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onAddExerciseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_exercise_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Exercise", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Move", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Visual Progress Chart ---
        if (exercises.isNotEmpty()) {
            item {
                ExerciseProgressChartCard(
                    exercises = exercises,
                    allWorkoutLogs = allWorkoutLogs
                )
            }
        }

        // --- Adjustable Push-Ups Tracking Mechanism ---
        item {
            AdjustablePushUpTrackerCard(
                todayWorkoutLogs = todayWorkoutLogs,
                onLogPushUps = { variation, reps, loadKg, diff ->
                    val ex = exercises.find { it.name.contains("Push", ignoreCase = true) }
                    val exId = ex?.id ?: 1L
                    val exName = "$variation Push-Ups"
                    onLogSet(exId, exName, loadKg, reps, diff)
                }
            )
        }

        // --- Hold & Isometric Stopwatch / Timer ---
        item {
            HoldTimerCard(
                onLogHoldSet = { exerciseName, secondsHold, difficulty ->
                    val ex = exercises.find { it.name.contains(exerciseName, ignoreCase = true) }
                    val exId = ex?.id ?: 2L
                    onLogSet(exId, exerciseName, 0.0, secondsHold, difficulty)
                }
            )
        }

        // --- Category Filters ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = cat
                            val newFiltered = if (cat == "All") exercises else exercises.filter { it.category.equals(cat, ignoreCase = true) }
                            selectedExercise = newFiltered.firstOrNull()
                        },
                        label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MassOrangePrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        )
                    )
                }
            }
        }

        // --- Set Logger Selector Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LOG A WORKING SET",
                        style = MaterialTheme.typography.labelSmall,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredExercises.isNotEmpty()) {
                        ExerciseSelectorRow(
                            exercises = filteredExercises,
                            selectedExercise = selectedExercise,
                            onSelect = { selectedExercise = it }
                        )
                    } else {
                        Text(
                            text = "No exercises in $selectedCategory. Add one using + New Move!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Difficulty selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Level:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        listOf("Beginner", "Medium", "Hard", "Backpack").forEach { level ->
                            val isSel = level == inputDifficulty
                            FilterChip(
                                selected = isSel,
                                onClick = { inputDifficulty = level },
                                label = { Text(level, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MassOrangePrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = MassOrangePrimary,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Inputs: Load and Reps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it },
                            label = { Text("Load / Backpack (kg)") },
                            placeholder = { Text("0 for Bodyweight") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_weight"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MassOrangePrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedLabelColor = MassOrangePrimary,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        OutlinedTextField(
                            value = inputReps,
                            onValueChange = { inputReps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_reps"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MassOrangePrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedLabelColor = MassOrangePrimary,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        Button(
                            onClick = {
                                val weight = inputWeight.toDoubleOrNull() ?: 0.0
                                val reps = inputReps.toIntOrNull()
                                val currentEx = selectedExercise
                                if (reps != null && currentEx != null) {
                                    onLogSet(currentEx.id, currentEx.name, weight, reps, inputDifficulty)
                                    inputWeight = ""
                                    inputReps = ""
                                }
                            },
                            enabled = inputReps.isNotBlank() && selectedExercise != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("log_set_button")
                        ) {
                            Text("Log Set", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Today's Logged Sets List ---
        item {
            Text(
                text = "Today's Completed Sets (${todayWorkoutLogs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (todayWorkoutLogs.isEmpty()) {
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
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "No Sets",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No workout sets logged today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "No gym equipment needed! Track reps, bodyweight leverage variations, or weighted backpack load.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            val groupedByExercise = todayWorkoutLogs.groupBy { it.exerciseName }
            items(groupedByExercise.keys.toList()) { exName ->
                val sets = groupedByExercise[exName] ?: emptyList()
                ExerciseSetsGroupCard(
                    exerciseName = exName,
                    sets = sets,
                    onDeleteSet = onDeleteSet
                )
            }
        }
    }
}

@Composable
fun ExerciseProgressChartCard(
    exercises: List<Exercise>,
    allWorkoutLogs: List<WorkoutLog>
) {
    var selectedExId by remember(exercises) {
        mutableStateOf(exercises.firstOrNull()?.id ?: 0L)
    }

    val selectedExercise = exercises.find { it.id == selectedExId } ?: exercises.firstOrNull()
    val logsForChart = remember(selectedExId, allWorkoutLogs) {
        allWorkoutLogs.filter { it.exerciseId == (selectedExercise?.id ?: 0L) || it.exerciseName.equals(selectedExercise?.name, true) }
            .groupBy { it.date }
            .map { (date, sets) ->
                val totalReps = sets.sumOf { it.repsCompleted }
                val maxLoad = sets.maxOfOrNull { it.weightKg } ?: 0.0
                Pair(date, Pair(totalReps, maxLoad))
            }
            .sortedBy { it.first }
            .takeLast(7)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MassOrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VISUAL PROGRESSION CHART",
                        style = MaterialTheme.typography.labelSmall,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Overload Trend (Reps & Load)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Icon(Icons.Default.ShowChart, contentDescription = "Chart", tint = MassOrangePrimary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exercise Selector for chart
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(exercises) { ex ->
                    val isSel = ex.id == selectedExercise?.id
                    Surface(
                        color = if (isSel) MassOrangePrimary else DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { selectedExId = ex.id }
                    ) {
                        Text(
                            text = ex.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) Color.Black else TextSecondary,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (logsForChart.isEmpty()) {
                Text(
                    text = "No previous set logs recorded for '${selectedExercise?.name}'. Log sets to see overload chart!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                val maxReps = logsForChart.maxOfOrNull { it.second.first }?.coerceAtLeast(1) ?: 1

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barWidth = width / (logsForChart.size * 2f)

                    logsForChart.forEachIndexed { index, (date, pair) ->
                        val (reps, load) = pair
                        val barHeight = (reps.toFloat() / maxReps.toFloat()) * (height * 0.8f)
                        val x = (index * (width / logsForChart.size)) + (width / logsForChart.size) / 2f

                        // Draw Reps Bar
                        drawRect(
                            color = MassOrangePrimary,
                            topLeft = Offset(x - barWidth / 2, height - barHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    logsForChart.forEach { (date, pair) ->
                        val (reps, load) = pair
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${reps}r", style = MaterialTheme.typography.labelSmall, color = MassOrangePrimary, fontWeight = FontWeight.Bold)
                            if (load > 0) {
                                Text(text = "${load.toInt()}kg", style = MaterialTheme.typography.labelSmall, color = ProgressBlue)
                            }
                            Text(text = date.takeLast(5), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseSelectorRow(
    exercises: List<Exercise>,
    selectedExercise: Exercise?,
    onSelect: (Exercise) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(exercises, key = { it.id }) { ex ->
            val isSelected = ex.id == selectedExercise?.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MassOrangePrimary.copy(alpha = 0.2f) else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (isSelected) MassOrangePrimary else DarkBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(ex) }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = ex.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MassOrangePrimary else TextPrimary
                    )
                    Text(
                        text = "Target: ${ex.targetSets}x${ex.targetReps} (${ex.difficultyLevel})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseSetsGroupCard(
    exerciseName: String,
    sets: List<WorkoutLog>,
    onDeleteSet: (Long) -> Unit
) {
    val totalVol = sets.sumOf { it.weightKg * it.repsCompleted }
    val maxWeight = sets.maxOfOrNull { it.weightKg } ?: 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Top Load: ${maxWeight}kg | Vol: ${totalVol.toInt()}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MassOrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            sets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${set.setNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${set.weightKg} kg × ${set.repsCompleted} reps (${set.difficultyLevel})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (set.isPersonalRecord) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GoldPR.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PR 🔥",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPR,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { onDeleteSet(set.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete Set",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustablePushUpTrackerCard(
    todayWorkoutLogs: List<WorkoutLog>,
    onLogPushUps: (variation: String, reps: Int, loadKg: Double, difficulty: String) -> Unit
) {
    var selectedVariation by remember { mutableStateOf("Feet-Elevated") }
    var currentReps by remember { mutableStateOf(15) }
    var backpackLoadKg by remember { mutableStateOf("0.0") }
    var targetDailyPushups by remember { mutableStateOf(100) }
    var difficultyLevel by remember { mutableStateOf("Medium") }

    val todayPushUpReps = remember(todayWorkoutLogs) {
        todayWorkoutLogs.filter { it.exerciseName.contains("Push", ignoreCase = true) }
            .sumOf { it.repsCompleted }
    }

    val progressFraction = (todayPushUpReps.toFloat() / targetDailyPushups.coerceAtLeast(1)).coerceIn(0f, 1f)
    val variations = listOf("Standard", "Feet-Elevated", "Diamond", "Pike", "Archer", "Backpack Loaded")

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MassOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ADJUSTABLE PUSH-UPS TRACKER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Volume & Variation Controller",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = MassOrangePrimary.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Push-ups",
                            tint = MassOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Push-Up Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Push-Ups: $todayPushUpReps / $targetDailyPushups reps",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "${(progressFraction * 100).toInt()}% Goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MassOrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MassOrangePrimary,
                trackColor = DarkSurfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Variations Chips
            Text("Select Variation:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(variations) { v ->
                    val isSel = v == selectedVariation
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedVariation = v },
                        label = { Text(v, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MassOrangePrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Adjustable Counter Control Box
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reps to Log",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = { currentReps = (currentReps - 5).coerceAtLeast(1) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurface)
                        ) {
                            Text("-5", color = MassOrangePrimary, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { currentReps = (currentReps - 1).coerceAtLeast(1) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurface)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus 1", tint = MassOrangePrimary)
                        }

                        Text(
                            text = "$currentReps",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        IconButton(
                            onClick = { currentReps += 1 },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurface)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus 1", tint = MassOrangePrimary)
                        }

                        IconButton(
                            onClick = { currentReps += 5 },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurface)
                        ) {
                            Text("+5", color = MassOrangePrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = backpackLoadKg,
                            onValueChange = { backpackLoadKg = it },
                            label = { Text("Added Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val load = backpackLoadKg.toDoubleOrNull() ?: 0.0
                                onLogPushUps(selectedVariation, currentReps, load, difficultyLevel)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .testTag("log_pushup_set_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Log Set", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log $currentReps Reps", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HoldTimerCard(
    onLogHoldSet: (exerciseName: String, secondsHold: Int, difficulty: String) -> Unit
) {
    var selectedHoldExercise by remember { mutableStateOf("Plank") }
    var isTimerRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var difficultyLevel by remember { mutableStateOf("Medium") }

    val holdExercises = listOf("Plank", "Dead Hang", "Wall Sit", "Hollow Hold", "Custom Hold")

    // Timer coroutine loop
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            kotlinx.coroutines.delay(1000L)
            elapsedSeconds += 1
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ProgressBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ISOMETRIC & HOLD STOPWATCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProgressBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Plank & Hanging Hold Timer ⏱️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = ProgressBlue.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Hold Timer",
                            tint = ProgressBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hold Exercise selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(holdExercises) { hold ->
                    val isSel = hold == selectedHoldExercise
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedHoldExercise = hold },
                        label = { Text(hold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ProgressBlue,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timer Display Circle / Digital Clock
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedHoldExercise.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = ProgressBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) ProgressBlue else TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Controls: Start/Pause, Reset, Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isTimerRunning = !isTimerRunning },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) MassOrangePrimary else ProgressBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("hold_timer_toggle")
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) "Pause" else "Start",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTimerRunning) "Pause" else "Start Timer",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isTimerRunning = false
                                elapsedSeconds = 0
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Quick Time buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(30, 45, 60, 90).forEach { sec ->
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable {
                                    elapsedSeconds = sec
                                    isTimerRunning = true
                                }
                            ) {
                                Text(
                                    text = "+${sec}s Preset",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (elapsedSeconds > 0) {
                                onLogHoldSet(selectedHoldExercise, elapsedSeconds, difficultyLevel)
                                isTimerRunning = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        enabled = elapsedSeconds > 0,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_hold_set_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Log Hold", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Log ${elapsedSeconds}s $selectedHoldExercise Hold",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

