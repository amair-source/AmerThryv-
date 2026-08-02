package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UserGoals
import com.example.ui.theme.*

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (name: String, mealType: String, calories: Int, protein: Int, carbs: Int, fat: Int, cost: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Breakfast") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Mass Shake")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Log Meal / Mass Snack", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meal Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_meal_name_field")
                )

                // Meal Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mealTypes.take(3).forEach { type ->
                        FilterChip(
                            selected = mealType == type,
                            onClick = { mealType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MassOrangePrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mealTypes.drop(3).forEach { type ->
                        FilterChip(
                            selected = mealType == type,
                            onClick = { mealType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MassOrangePrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("add_meal_calories_field")
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("add_meal_protein_field")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Meal Cost ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calories.toIntOrNull() ?: 0
                    val p = protein.toIntOrNull() ?: 0
                    val c = carbs.toIntOrNull() ?: 0
                    val f = fat.toIntOrNull() ?: 0
                    val cst = cost.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && cal > 0) {
                        onAddMeal(name, mealType, cal, p, c, f, cst)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                modifier = Modifier.testTag("submit_meal_button")
            ) {
                Text("Add Meal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onAddRecipe: (name: String, description: String, isShake: Boolean, calories: Int, protein: Int, carbs: Int, fat: Int, cost: Double, ingredientsJson: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isShake by remember { mutableStateOf(false) }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Create Custom Recipe / Shake", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipe Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_recipe_name_field")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isShake,
                        onCheckedChange = { isShake = it },
                        colors = CheckboxDefaults.colors(checkedColor = MassOrangePrimary)
                    )
                    Text("Tag as Mass Gainer Shake 🔥", color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Estimated Cost ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calories.toIntOrNull() ?: 0
                    val p = protein.toIntOrNull() ?: 0
                    val c = carbs.toIntOrNull() ?: 0
                    val f = fat.toIntOrNull() ?: 0
                    val cst = cost.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && cal > 0) {
                        onAddRecipe(name, description, isShake, cal, p, c, f, cst, "[]")
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                modifier = Modifier.testTag("submit_recipe_button")
            ) {
                Text("Save Recipe", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onAddExercise: (name: String, category: String, targetSets: Int, targetReps: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Chest") }
    var sets by remember { mutableStateOf("4") }
    var reps by remember { mutableStateOf("12") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Chest", "Legs", "Back", "Shoulders", "Arms", "Core")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Home / Calisthenics Movement", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g. Archer Push-ups, Chair Split Squat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_exercise_name_field")
                )

                Text("Category:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MassOrangePrimary, selectedLabelColor = Color.Black)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MassOrangePrimary, selectedLabelColor = Color.Black)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("Target Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Target Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Cue") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = sets.toIntOrNull() ?: 4
                    val r = reps.toIntOrNull() ?: 8
                    if (name.isNotBlank()) {
                        onAddExercise(name, category, s, r, notes)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                modifier = Modifier.testTag("submit_exercise_button")
            ) {
                Text("Add Movement", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddRoutineDayDialog(
    onDismiss: () -> Unit,
    onAddDay: (dayName: String, category: String, description: String) -> Unit
) {
    var dayName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Workout") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("Workout", "Bulk Nutrition", "Recovery", "Custom")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Create Routine Day", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = dayName,
                    onValueChange = { dayName = it },
                    label = { Text("Day / Routine Name") },
                    placeholder = { Text("e.g., Chest & Calisthenics Day, High Calorie Bulk Day") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_routine_day_name")
                )

                Text("Category:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MassOrangePrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Objective") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dayName.isNotBlank()) {
                        onAddDay(dayName, category, description)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                modifier = Modifier.testTag("submit_routine_day")
            ) {
                Text("Create Day", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddRoutineTaskDialog(
    dayId: Long,
    onDismiss: () -> Unit,
    onAddTask: (dayId: Long, mainTaskName: String, itemType: String, difficultyLevel: String, targetSetsReps: String, subtasksJson: String) -> Unit
) {
    var mainTaskName by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("Exercise") }
    var difficultyLevel by remember { mutableStateOf("Medium") }
    var targetSetsReps by remember { mutableStateOf("") }
    var subtasksInput by remember { mutableStateOf("") }

    val types = listOf("Exercise", "Shake", "Meal", "Custom")
    val levels = listOf("Easy", "Medium", "Hard")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Task / Exercise Split Item", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mainTaskName,
                    onValueChange = { mainTaskName = it },
                    label = { Text("Task / Exercise Name") },
                    placeholder = { Text("e.g. Feet-Elevated Push-ups, Morning 1000kcal Shake") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_routine_task_name")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Type:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        types.take(2).forEach { t ->
                            FilterChip(
                                selected = itemType == t,
                                onClick = { itemType = t },
                                label = { Text(t) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MassOrangePrimary, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Difficulty:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        levels.forEach { lvl ->
                            FilterChip(
                                selected = difficultyLevel == lvl,
                                onClick = { difficultyLevel = lvl },
                                label = { Text(lvl) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MassOrangePrimary, selectedLabelColor = Color.Black)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetSetsReps,
                    onValueChange = { targetSetsReps = it },
                    label = { Text("Target Sets/Reps or Portion") },
                    placeholder = { Text("e.g. 4x12 reps, 1 Large Glass") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtasksInput,
                    onValueChange = { subtasksInput = it },
                    label = { Text("Sub-tasks (comma-separated)") },
                    placeholder = { Text("e.g. Warmup wrist, Squeeze chest 2s, Log reps") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mainTaskName.isNotBlank()) {
                        val subtasksList = subtasksInput.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        val jsonArray = org.json.JSONArray(subtasksList).toString()

                        onAddTask(dayId, mainTaskName, itemType, difficultyLevel, targetSetsReps, jsonArray)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                modifier = Modifier.testTag("submit_routine_task")
            ) {
                Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun SettingsDialog(
    userGoals: UserGoals,
    onDismiss: () -> Unit,
    onSaveGoals: (calories: Int, protein: Int, carbs: Int, fat: Int, currentWeight: Double, targetWeight: Double) -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    var calories by remember { mutableStateOf(userGoals.targetCalories.toString()) }
    var protein by remember { mutableStateOf(userGoals.targetProtein.toString()) }
    var carbs by remember { mutableStateOf(userGoals.targetCarbs.toString()) }
    var fat by remember { mutableStateOf(userGoals.targetFat.toString()) }
    var currentWeight by remember { mutableStateOf(userGoals.currentWeightKg.toString()) }
    var targetWeight by remember { mutableStateOf(userGoals.targetWeightKg.toString()) }

    var apiKeyInput by remember { mutableStateOf(userGoals.customApiKey) }
    var showApiKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Settings & BYO Gemini Key", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DAILY TARGET MACROS & WEIGHT", style = MaterialTheme.typography.labelSmall, color = MassOrangePrimary, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentWeight,
                        onValueChange = { currentWeight = it },
                        label = { Text("Current (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Goal (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text("BYO GEMINI API KEY (STORED LOCALLY)", style = MaterialTheme.typography.labelSmall, color = MassOrangePrimary, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Gemini API Key") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = MassOrangePrimary) },
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle key"
                            )
                        }
                    },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("api_key_field")
                )

                Text(
                    text = "Key is stored in Local Preferences. If left blank, defaults to injected runtime key.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calories.toIntOrNull() ?: 3200
                    val p = protein.toIntOrNull() ?: 180
                    val c = carbs.toIntOrNull() ?: 400
                    val f = fat.toIntOrNull() ?: 90
                    val cw = currentWeight.toDoubleOrNull() ?: 72.5
                    val tw = targetWeight.toDoubleOrNull() ?: 80.0

                    onSaveGoals(cal, p, c, f, cw, tw)
                    onSaveApiKey(apiKeyInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Save Settings", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
