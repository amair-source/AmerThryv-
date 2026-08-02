package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MealLog
import com.example.data.db.Recipe
import com.example.data.db.WorkoutLog
import com.example.data.preferences.UserGoals
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    userGoals: UserGoals,
    todayMeals: List<MealLog>,
    todayWorkouts: List<WorkoutLog>,
    recipes: List<Recipe>,
    onQuickLogShake: (Recipe) -> Unit,
    onAddMealClick: () -> Unit,
    onDeleteMeal: (Long) -> Unit
) {
    val totalCalories = todayMeals.sumOf { it.calories }
    val totalProtein = todayMeals.sumOf { it.proteinGrams }
    val totalCarbs = todayMeals.sumOf { it.carbsGrams }
    val totalFat = todayMeals.sumOf { it.fatGrams }
    val totalDailyCost = todayMeals.sumOf { it.cost }

    val calorieProgress = (totalCalories.toFloat() / userGoals.targetCalories).coerceIn(0f, 1f)
    val prCount = todayWorkouts.count { it.isPersonalRecord }
    val totalVolumeKg = todayWorkouts.sumOf { it.weightKg * it.repsCompleted }

    val massShake = recipes.firstOrNull { it.isMassGainerShake } ?: recipes.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Top Weight Progress Banner ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEIGHT GAIN PROGRESSION",
                                style = MaterialTheme.typography.labelMedium,
                                color = MassOrangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${userGoals.currentWeightKg} kg  ➔  ${userGoals.targetWeightKg} kg Goal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            color = MassOrangePrimary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Weight Goal",
                                tint = MassOrangePrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val weightGained = (userGoals.currentWeightKg - 65.0).coerceAtLeast(0.0) // baseline demo calculation
                    val totalToGain = (userGoals.targetWeightKg - 65.0).coerceAtLeast(1.0)
                    val weightProgress = (weightGained / totalToGain).toFloat().coerceIn(0f, 1f)
                    
                    LinearProgressIndicator(
                        progress = { weightProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MassOrangePrimary,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }

        // --- Calorie Surplus & Macros Summary Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Calories",
                                tint = MacroCaloriesColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Daily Calorie Surplus",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Est. Daily Food Spent: \$${String.format("%.2f", totalDailyCost)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                        }
                        Text(
                            text = "$totalCalories / ${userGoals.targetCalories} kcal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MacroCaloriesColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { calorieProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MacroCaloriesColor,
                        trackColor = DarkSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Macros Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroBox(
                            modifier = Modifier.weight(1f),
                            title = "Protein",
                            current = totalProtein,
                            target = userGoals.targetProtein,
                            unit = "g",
                            color = MacroProteinColor
                        )
                        MacroBox(
                            modifier = Modifier.weight(1f),
                            title = "Carbs",
                            current = totalCarbs,
                            target = userGoals.targetCarbs,
                            unit = "g",
                            color = MacroCarbsColor
                        )
                        MacroBox(
                            modifier = Modifier.weight(1f),
                            title = "Fats",
                            current = totalFat,
                            target = userGoals.targetFat,
                            unit = "g",
                            color = MacroFatColor
                        )
                    }
                }
            }
        }

        // --- Quick Mass Gainer Shake Trigger ---
        if (massShake != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MassOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onQuickLogShake(massShake) }
                        .testTag("quick_mass_shake_card")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = MassOrangePrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Mass Shake",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "QUICK MASS SHAKE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MassOrangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = massShake.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "+${massShake.totalCalories} kcal | P:${massShake.totalProtein}g C:${massShake.totalCarbs}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { onQuickLogShake(massShake) },
                            colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Now", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Reminders & Push Notifications Controller ---
        item {
            RemindersAndNotificationsCard()
        }

        // --- Progressive Overload Today Summary Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Today's Sets",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${todayWorkouts.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Volume Pumped",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${totalVolumeKg.toInt()} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MassOrangePrimary
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PRs Smashed",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = if (prCount > 0) "$prCount 🔥" else "0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldPR
                        )
                    }
                }
            }
        }

        // --- Today's Meals Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Meals (${todayMeals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onAddMealClick,
                    modifier = Modifier.testTag("add_meal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Meal",
                        tint = MassOrangePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Meals List
        if (todayMeals.isEmpty()) {
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
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "No Meals",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals logged for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Tap + or Quick Log Mass Shake to start hitting your surplus!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(todayMeals, key = { it.id }) { meal ->
                MealItemCard(meal = meal, onDelete = { onDeleteMeal(meal.id) })
            }
        }
    }
}

@Composable
fun MacroBox(
    modifier: Modifier = Modifier,
    title: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$current/$target$unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = DarkBorder
            )
        }
    }
}

@Composable
fun MealItemCard(
    meal: MealLog,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.mealType.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MassOrangePrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${meal.calories} kcal", style = MaterialTheme.typography.bodySmall, color = MacroCaloriesColor, fontWeight = FontWeight.Bold)
                    Text(text = "P: ${meal.proteinGrams}g", style = MaterialTheme.typography.bodySmall, color = MacroProteinColor)
                    Text(text = "C: ${meal.carbsGrams}g", style = MaterialTheme.typography.bodySmall, color = MacroCarbsColor)
                    Text(text = "F: ${meal.fatGrams}g", style = MaterialTheme.typography.bodySmall, color = MacroFatColor)
                    if (meal.cost > 0) {
                        Text(text = "|\$${String.format("%.2f", meal.cost)}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Meal",
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
fun RemindersAndNotificationsCard() {
    var mealRemindersEnabled by remember { mutableStateOf(true) }
    var workoutRemindersEnabled by remember { mutableStateOf(true) }
    var motivationEnabled by remember { mutableStateOf(true) }

    var notificationBanner by remember { mutableStateOf<Pair<String, String>?>(null) }

    val motivationalQuotes = remember {
        listOf(
            "Calisthenics builds raw functional strength without commercial gym clutter! 🔥",
            "Consistency beats intensity. Eat your surplus and hit your push-up sets today!",
            "Progressive overload at home: add reps, elevate your feet, or add a backpack load!",
            "1,000 kcal Mass Shakes make weight gain effortless on a busy schedule. 🥛"
        )
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PUSH NOTIFICATIONS & REMINDERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Meal, Workout & Motivation Alerts 🔔",
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notifications",
                            tint = MassOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Notification Banner Simulation Popup
            notificationBanner?.let { (title, msg) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, SuccessGreen, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }

                        IconButton(onClick = { notificationBanner = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted)
                        }
                    }
                }
            }

            // Reminders List
            // 1. Meals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Meal & Shake Reminders", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("08:00 AM, 01:00 PM, 07:00 PM", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = mealRemindersEnabled,
                    onCheckedChange = { mealRemindersEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MassOrangePrimary)
                )
            }

            Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 6.dp))

            // 2. Calisthenics & Push-ups
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Home Calisthenics Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Daily 05:00 PM (Push-Ups & Hold Timer)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = workoutRemindersEnabled,
                    onCheckedChange = { workoutRemindersEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MassOrangePrimary)
                )
            }

            Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 6.dp))

            // 3. Daily Motivation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Motivation Boost", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Morning mindset quote & surplus reminder", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = motivationEnabled,
                    onCheckedChange = { motivationEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MassOrangePrimary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons to test push notification alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val quote = motivationalQuotes.random()
                        notificationBanner = Pair("⚡ Motivational Push Alert", quote)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FormatQuote, contentDescription = "Quote", tint = MassOrangePrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Motivation", color = MassOrangePrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        notificationBanner = Pair("🥛 Time for your Mass Shake!", "Log your afternoon 900kcal shake to maintain daily calorie surplus.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("test_push_notification_button")
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Test Notification", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Push Alert", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

