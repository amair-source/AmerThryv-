package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MassTrackerTheme {
                MassTrackerApp(viewModel)
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ROUTINES("Routines", Icons.Default.EventNote),
    WORKOUT("Workouts", Icons.Default.FitnessCenter),
    RECIPES("Recipes", Icons.Default.Blender),
    ACHIEVEMENTS("Badges", Icons.Default.EmojiEvents),
    COACH("AI Coach", Icons.Default.SmartToy)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MassTrackerApp(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    val todayMeals by viewModel.todayMeals.collectAsStateWithLifecycle()
    val todayWorkouts by viewModel.todayWorkoutLogs.collectAsStateWithLifecycle()
    val allWorkouts by viewModel.allWorkoutLogs.collectAsStateWithLifecycle()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val routineDays by viewModel.routineDays.collectAsStateWithLifecycle()
    val routineTasks by viewModel.routineTasks.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var showAddMealDialog by remember { mutableStateOf(false) }
    var showAddRecipeDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showAddRoutineDayDialog by remember { mutableStateOf(false) }
    var showAddRoutineTaskDialogForDayId by remember { mutableStateOf<Long?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = "AMERTHRYV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MassOrangePrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Home / Non-Gym",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MassOrangePrimary else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) MassOrangePrimary else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MassOrangePrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    userGoals = userGoals,
                    todayMeals = todayMeals,
                    todayWorkouts = todayWorkouts,
                    recipes = recipes,
                    onQuickLogShake = { shakeRecipe ->
                        viewModel.quickLogRecipeAsMeal(shakeRecipe)
                    },
                    onAddMealClick = { showAddMealDialog = true },
                    onDeleteMeal = { id -> viewModel.deleteMeal(id) }
                )

                NavigationTab.ROUTINES -> RoutinesScreen(
                    routineDays = routineDays,
                    routineTasks = routineTasks,
                    onAddDayClick = { showAddRoutineDayDialog = true },
                    onAddTaskClick = { dayId -> showAddRoutineTaskDialogForDayId = dayId },
                    onToggleTaskCompleted = { task -> viewModel.toggleTaskCompleted(task) },
                    onDeleteDay = { dayId -> viewModel.deleteRoutineDay(dayId) },
                    onDeleteTask = { taskId -> viewModel.deleteRoutineTask(taskId) }
                )

                NavigationTab.WORKOUT -> WorkoutScreen(
                    exercises = exercises,
                    todayWorkoutLogs = todayWorkouts,
                    allWorkoutLogs = allWorkouts,
                    onLogSet = { exId, exName, weight, reps, diff ->
                        viewModel.logWorkoutSet(exId, exName, weight, reps, diff)
                    },
                    onDeleteSet = { setId -> viewModel.deleteWorkoutSet(setId) },
                    onAddExerciseClick = { showAddExerciseDialog = true }
                )

                NavigationTab.RECIPES -> RecipesScreen(
                    recipes = recipes,
                    onLogRecipeAsMeal = { recipe ->
                        viewModel.quickLogRecipeAsMeal(recipe)
                    },
                    onAddRecipeClick = { showAddRecipeDialog = true },
                    onDeleteRecipe = { recipeId -> viewModel.deleteRecipe(recipeId) }
                )

                NavigationTab.ACHIEVEMENTS -> AchievementsScreen(
                    achievements = achievements
                )

                NavigationTab.COACH -> AiCoachScreen(
                    userGoals = userGoals,
                    chatMessages = chatMessages,
                    isLoading = isChatLoading,
                    onSendMessage = { prompt -> viewModel.sendChatMessage(prompt) },
                    onClearChat = { viewModel.clearChat() },
                    onOpenSettingsClick = { showSettingsDialog = true }
                )
            }
        }
    }

    // --- Dialogs ---
    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onAddMeal = { name, type, cal, p, c, f, cost ->
                viewModel.addMeal(name, type, cal, p, c, f, cost)
            }
        )
    }

    if (showAddRecipeDialog) {
        AddRecipeDialog(
            onDismiss = { showAddRecipeDialog = false },
            onAddRecipe = { name, desc, isShake, cal, p, c, f, cost, json ->
                viewModel.addRecipe(name, desc, isShake, cal, p, c, f, cost, json)
            }
        )
    }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onAddExercise = { name, category, sets, reps, notes ->
                viewModel.addExercise(name, category, sets, reps, notes = notes)
            }
        )
    }

    if (showAddRoutineDayDialog) {
        AddRoutineDayDialog(
            onDismiss = { showAddRoutineDayDialog = false },
            onAddDay = { name, cat, desc ->
                viewModel.addRoutineDay(name, cat, desc)
            }
        )
    }

    showAddRoutineTaskDialogForDayId?.let { dayId ->
        AddRoutineTaskDialog(
            dayId = dayId,
            onDismiss = { showAddRoutineTaskDialogForDayId = null },
            onAddTask = { dId, name, type, lvl, target, subJson ->
                viewModel.addRoutineTask(dId, name, type, lvl, target, subJson)
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            userGoals = userGoals,
            onDismiss = { showSettingsDialog = false },
            onSaveGoals = { cal, p, c, f, cw, tw ->
                viewModel.updateGoals(cal, p, c, f, cw, tw)
            },
            onSaveApiKey = { key ->
                viewModel.saveApiKey(key)
            }
        )
    }
}

