package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.preferences.UserGoals
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    val repository = MassRepository(db, preferencesRepository)

    val todayDate: String = repository.getTodayString()

    val userGoals: StateFlow<UserGoals> = repository.userGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserGoals()
    )

    val todayMeals: StateFlow<List<MealLog>> = repository.getTodayMeals(todayDate).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.getTodayWorkoutLogs(todayDate).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.allRecentWorkoutLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recipes: StateFlow<List<Recipe>> = repository.allRecipes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val exercises: StateFlow<List<Exercise>> = repository.allExercises.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routineDays: StateFlow<List<RoutineDay>> = repository.allRoutineDays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routineTasks: StateFlow<List<RoutineTask>> = repository.allRoutineTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val achievements: StateFlow<List<AchievementRecord>> = repository.allAchievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Meals ---
    fun addMeal(
        name: String,
        mealType: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        cost: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addMealLog(
                MealLog(
                    date = todayDate,
                    mealType = mealType,
                    name = name,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    cost = cost,
                    notes = notes
                )
            )
        }
    }

    fun quickLogRecipeAsMeal(recipe: Recipe) {
        viewModelScope.launch {
            repository.addMealLog(
                MealLog(
                    date = todayDate,
                    mealType = if (recipe.isMassGainerShake) "Mass Shake" else "Recipe Meal",
                    name = recipe.name,
                    calories = recipe.totalCalories,
                    proteinGrams = recipe.totalProtein,
                    carbsGrams = recipe.totalCarbs,
                    fatGrams = recipe.totalFat,
                    cost = recipe.estimatedCost,
                    notes = "Logged from recipe: ${recipe.description}"
                )
            )
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMealLog(mealId)
        }
    }

    // --- Recipes ---
    fun addRecipe(
        name: String,
        description: String,
        isMassGainerShake: Boolean,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        cost: Double,
        ingredientsJson: String
    ) {
        viewModelScope.launch {
            repository.addRecipe(
                Recipe(
                    name = name,
                    description = description,
                    isMassGainerShake = isMassGainerShake,
                    totalCalories = calories,
                    totalProtein = protein,
                    totalCarbs = carbs,
                    totalFat = fat,
                    estimatedCost = cost,
                    ingredientsJson = ingredientsJson
                )
            )
        }
    }

    fun deleteRecipe(recipeId: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }

    // --- Exercises ---
    fun addExercise(
        name: String,
        category: String,
        targetSets: Int,
        targetReps: Int,
        difficultyLevel: String = "Medium",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addExercise(
                Exercise(
                    name = name,
                    category = category,
                    targetSets = targetSets,
                    targetReps = targetReps,
                    difficultyLevel = difficultyLevel,
                    notes = notes
                )
            )
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
        }
    }

    // --- Workout Sets & Progressive Overload ---
    fun logWorkoutSet(
        exerciseId: Long,
        exerciseName: String,
        weightKg: Double,
        reps: Int,
        difficultyLevel: String = "Medium",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val existingToday = todayWorkoutLogs.value.filter { it.exerciseId == exerciseId }
            val nextSetNumber = existingToday.size + 1

            val previousLogs = repository.getLogsForExercise(exerciseId).first()
            val maxPreviousWeight = previousLogs.maxOfOrNull { it.weightKg } ?: 0.0
            val isPR = weightKg > maxPreviousWeight && maxPreviousWeight > 0

            repository.addWorkoutLog(
                WorkoutLog(
                    date = todayDate,
                    exerciseId = exerciseId,
                    exerciseName = exerciseName,
                    setNumber = nextSetNumber,
                    weightKg = weightKg,
                    repsCompleted = reps,
                    difficultyLevel = difficultyLevel,
                    isPersonalRecord = isPR,
                    notes = notes
                )
            )
        }
    }

    fun deleteWorkoutSet(setId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(setId)
        }
    }

    // --- Routines & Days ---
    fun addRoutineDay(dayName: String, category: String, description: String) {
        viewModelScope.launch {
            repository.addRoutineDay(RoutineDay(dayName = dayName, category = category, description = description))
        }
    }

    fun deleteRoutineDay(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutineDay(id)
        }
    }

    fun addRoutineTask(
        dayId: Long,
        mainTaskName: String,
        itemType: String,
        difficultyLevel: String,
        targetSetsReps: String,
        subtasksJson: String
    ) {
        viewModelScope.launch {
            repository.addRoutineTask(
                RoutineTask(
                    dayId = dayId,
                    mainTaskName = mainTaskName,
                    itemType = itemType,
                    difficultyLevel = difficultyLevel,
                    targetSetsReps = targetSetsReps,
                    subtasksJson = subtasksJson
                )
            )
        }
    }

    fun toggleTaskCompleted(task: RoutineTask) {
        viewModelScope.launch {
            repository.updateRoutineTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteRoutineTask(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutineTask(id)
        }
    }

    // --- User Goals & API Key ---
    fun updateGoals(
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        currentWeight: Double,
        targetWeight: Double
    ) {
        viewModelScope.launch {
            repository.updateUserGoals(calories, protein, carbs, fat, currentWeight, targetWeight)
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            repository.saveApiKey(apiKey)
        }
    }

    // --- Chat ---
    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank() || _isChatLoading.value) return
        viewModelScope.launch {
            _isChatLoading.value = true
            repository.sendChatMessage(prompt)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }
}

