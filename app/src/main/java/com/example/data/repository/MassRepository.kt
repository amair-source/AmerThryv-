package com.example.data.repository

import com.example.data.db.*
import com.example.data.network.GeminiClient
import com.example.data.preferences.UserGoals
import com.example.data.preferences.UserPreferencesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MassRepository(
    private val db: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    val mealLogDao = db.mealLogDao()
    val recipeDao = db.recipeDao()
    val exerciseDao = db.exerciseDao()
    val workoutLogDao = db.workoutLogDao()
    val routineDayDao = db.routineDayDao()
    val routineTaskDao = db.routineTaskDao()
    val achievementDao = db.achievementDao()
    val chatMessageDao = db.chatMessageDao()

    val userGoals: Flow<UserGoals> = preferencesRepository.userGoals
    val geminiClient = GeminiClient()

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Meals ---
    fun getTodayMeals(date: String = getTodayString()): Flow<List<MealLog>> {
        return mealLogDao.getMealLogsByDate(date)
    }

    val allRecentMealLogs: Flow<List<MealLog>> = mealLogDao.getAllRecentMealLogs()

    suspend fun addMealLog(mealLog: MealLog) {
        mealLogDao.insertMealLog(mealLog)
        checkAndUnlockAchievements()
    }

    suspend fun updateMealLog(mealLog: MealLog) {
        mealLogDao.updateMealLog(mealLog)
    }

    suspend fun deleteMealLog(id: Long) {
        mealLogDao.deleteMealLogById(id)
    }

    // --- Recipes ---
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()
    val massGainerShakes: Flow<List<Recipe>> = recipeDao.getMassGainerShakes()

    suspend fun addRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
        checkAndUnlockAchievements()
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteRecipeById(id)
    }

    // --- Exercises ---
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
    }

    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise)
    }

    suspend fun deleteExercise(id: Long) {
        exerciseDao.deleteExerciseById(id)
    }

    // --- Workout Logs ---
    fun getTodayWorkoutLogs(date: String = getTodayString()): Flow<List<WorkoutLog>> {
        return workoutLogDao.getWorkoutLogsByDate(date)
    }

    fun getLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>> {
        return workoutLogDao.getLogsForExercise(exerciseId)
    }

    val allRecentWorkoutLogs: Flow<List<WorkoutLog>> = workoutLogDao.getAllRecentWorkoutLogs()

    suspend fun addWorkoutLog(log: WorkoutLog) {
        workoutLogDao.insertWorkoutLog(log)
        checkAndUnlockAchievements()
    }

    suspend fun updateWorkoutLog(log: WorkoutLog) {
        workoutLogDao.updateWorkoutLog(log)
    }

    suspend fun deleteWorkoutLog(id: Long) {
        workoutLogDao.deleteWorkoutLogById(id)
    }

    // --- Routine Days & Tasks ---
    val allRoutineDays: Flow<List<RoutineDay>> = routineDayDao.getAllRoutineDays()
    val allRoutineTasks: Flow<List<RoutineTask>> = routineTaskDao.getAllRoutineTasks()

    fun getTasksForDay(dayId: Long): Flow<List<RoutineTask>> {
        return routineTaskDao.getTasksForDay(dayId)
    }

    suspend fun addRoutineDay(day: RoutineDay): Long {
        return routineDayDao.insertRoutineDay(day)
    }

    suspend fun updateRoutineDay(day: RoutineDay) {
        routineDayDao.updateRoutineDay(day)
    }

    suspend fun deleteRoutineDay(id: Long) {
        routineDayDao.deleteRoutineDayById(id)
    }

    suspend fun addRoutineTask(task: RoutineTask): Long {
        return routineTaskDao.insertTask(task)
    }

    suspend fun updateRoutineTask(task: RoutineTask) {
        routineTaskDao.updateTask(task)
    }

    suspend fun deleteRoutineTask(id: Long) {
        routineTaskDao.deleteTaskById(id)
    }

    // --- Achievements ---
    val allAchievements: Flow<List<AchievementRecord>> = achievementDao.getAllAchievements()

    suspend fun checkAndUnlockAchievements() {
        val today = getTodayString()
        val goals = userGoals.first()
        val meals = getTodayMeals(today).first()
        val workoutLogs = allRecentWorkoutLogs.first()
        val recipes = allRecipes.first()

        val totalCal = meals.sumOf { it.calories }

        if (meals.isNotEmpty()) {
            achievementDao.insertAchievement(
                AchievementRecord(
                    id = "FIRST_LOG",
                    title = "First Step into Mass",
                    description = "Logged your first meal or workout in Mass Pro",
                    category = "Logging",
                    isUnlocked = true,
                    unlockedDate = today
                )
            )
        }

        if (totalCal >= goals.targetCalories) {
            achievementDao.insertAchievement(
                AchievementRecord(
                    id = "CALORIES_3_DAYS",
                    title = "Surplus Master",
                    description = "Hit target calorie surplus 3 days in a row",
                    category = "Nutrition",
                    isUnlocked = true,
                    unlockedDate = today
                )
            )
        }

        if (workoutLogs.size >= 10) {
            achievementDao.insertAchievement(
                AchievementRecord(
                    id = "WORKOUT_10",
                    title = "Home Calisthenics Hero",
                    description = "Completed 10 home bodyweight workout sessions",
                    category = "Workouts",
                    isUnlocked = true,
                    unlockedDate = today
                )
            )
        }

        if (recipes.size >= 3) {
            achievementDao.insertAchievement(
                AchievementRecord(
                    id = "RECIPE_MASTER",
                    title = "Budget Mass Chef",
                    description = "Created 3 custom high-calorie recipes or mass gain shakes",
                    category = "Nutrition",
                    isUnlocked = true,
                    unlockedDate = today
                )
            )
        }

        if (goals.currentWeightKg >= goals.targetWeightKg - 2.0 && goals.currentWeightKg > 70.0) {
            achievementDao.insertAchievement(
                AchievementRecord(
                    id = "FIRST_2KG_GAIN",
                    title = "First 2kg Mass Milestone",
                    description = "Gained your first 2kg towards your target bodyweight",
                    category = "Mass Gain",
                    isUnlocked = true,
                    unlockedDate = today
                )
            )
        }
    }

    // --- Preferences ---
    suspend fun updateUserGoals(
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        currentWeight: Double,
        targetWeight: Double
    ) {
        preferencesRepository.updateUserGoals(calories, protein, carbs, fat, currentWeight, targetWeight)
        checkAndUnlockAchievements()
    }

    suspend fun saveApiKey(apiKey: String) {
        preferencesRepository.saveApiKey(apiKey)
    }

    // --- Chat Messages & AI Auto-Save ---
    val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun sendChatMessage(userText: String): Result<String> {
        chatMessageDao.insertMessage(
            ChatMessage(isUser = true, messageText = userText)
        )

        val report = compileContextReport()
        val goals = userGoals.first()

        val response = geminiClient.generateCoachResponse(
            customApiKey = goals.customApiKey,
            userPrompt = userText,
            contextReport = report
        )

        response.onSuccess { botText ->
            var autoActionText = ""
            try {
                autoActionText = parseAndAutoSaveAiAction(botText)
            } catch (e: Exception) {
                // Ignore json parse error if response had no action
            }

            chatMessageDao.insertMessage(
                ChatMessage(
                    isUser = false,
                    messageText = botText,
                    autoAppliedAction = autoActionText
                )
            )
        }.onFailure { err ->
            chatMessageDao.insertMessage(
                ChatMessage(
                    isUser = false,
                    messageText = "⚠️ Coach Error: ${err.localizedMessage ?: "Failed to connect to Gemini."}"
                )
            )
        }

        return response
    }

    private suspend fun parseAndAutoSaveAiAction(botText: String): String {
        if (!botText.contains("```json") && !botText.contains("{")) return ""

        val jsonStr = if (botText.contains("```json")) {
            botText.substringAfter("```json").substringBefore("```").trim()
        } else if (botText.contains("{") && botText.contains("}")) {
            "{" + botText.substringAfter("{").substringBeforeLast("}") + "}"
        } else {
            return ""
        }

        val json = JSONObject(jsonStr)
        val action = json.optString("action")

        return when (action) {
            "ADD_RECIPE" -> {
                val recObj = json.getJSONObject("recipe")
                val recipe = Recipe(
                    name = recObj.optString("name", "AI Mass Recipe"),
                    description = recObj.optString("description", "Generated by AI Coach"),
                    isMassGainerShake = recObj.optBoolean("isMassGainerShake", true),
                    totalCalories = recObj.optInt("totalCalories", 800),
                    totalProtein = recObj.optInt("totalProtein", 50),
                    totalCarbs = recObj.optInt("totalCarbs", 90),
                    totalFat = recObj.optInt("totalFat", 25),
                    estimatedCost = recObj.optDouble("estimatedCost", 2.50),
                    ingredientsJson = recObj.optString("ingredientsJson", "[]")
                )
                recipeDao.insertRecipe(recipe)
                "✨ Auto-saved recipe '${recipe.name}' to your Custom Recipes!"
            }
            "ADD_EXERCISE" -> {
                val exObj = json.getJSONObject("exercise")
                val exercise = Exercise(
                    name = exObj.optString("name", "AI Calisthenics Exercise"),
                    category = exObj.optString("category", "Chest"),
                    targetSets = exObj.optInt("targetSets", 4),
                    targetReps = exObj.optInt("targetReps", 12),
                    difficultyLevel = exObj.optString("difficultyLevel", "Medium"),
                    notes = exObj.optString("notes", "Generated by AI Coach")
                )
                exerciseDao.insertExercise(exercise)
                "✨ Auto-saved exercise '${exercise.name}' to your Workout Library!"
            }
            "ADD_MEAL_LOG" -> {
                val mealObj = json.getJSONObject("meal")
                val meal = MealLog(
                    date = getTodayString(),
                    mealType = mealObj.optString("mealType", "Mass Shake"),
                    name = mealObj.optString("name", "AI Bulk Meal"),
                    calories = mealObj.optInt("calories", 700),
                    proteinGrams = mealObj.optInt("proteinGrams", 45),
                    carbsGrams = mealObj.optInt("carbsGrams", 80),
                    fatGrams = mealObj.optInt("fatGrams", 20),
                    cost = mealObj.optDouble("cost", 2.50),
                    notes = mealObj.optString("notes", "Auto-logged by AI")
                )
                mealLogDao.insertMealLog(meal)
                "✨ Auto-logged meal '${meal.name}' (${meal.calories} kcal) to today's diary!"
            }
            "CREATE_ROUTINE_DAY" -> {
                val dayObj = json.getJSONObject("routineDay")
                val day = RoutineDay(
                    dayName = dayObj.optString("dayName", "AI Routine Day"),
                    category = dayObj.optString("category", "Workout"),
                    description = dayObj.optString("description", "Generated home split")
                )
                val dayId = routineDayDao.insertRoutineDay(day)
                val tasksArr = json.optJSONArray("tasks")
                if (tasksArr != null) {
                    for (i in 0 until tasksArr.length()) {
                        val taskObj = tasksArr.getJSONObject(i)
                        routineTaskDao.insertTask(
                            RoutineTask(
                                dayId = dayId,
                                mainTaskName = taskObj.optString("mainTaskName", "Home Task"),
                                itemType = taskObj.optString("itemType", "Exercise"),
                                difficultyLevel = taskObj.optString("difficultyLevel", "Medium"),
                                targetSetsReps = taskObj.optString("targetSetsReps", "4x12"),
                                subtasksJson = taskObj.optString("subtasksJson", "[]")
                            )
                        )
                    }
                }
                "✨ Created new Routine Day '${day.dayName}' with tasks!"
            }
            else -> ""
        }
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }

    // --- Context Generator for AI Coach ---
    suspend fun compileContextReport(): String {
        val today = getTodayString()
        val goals = userGoals.first()
        val todayMeals = mealLogDao.getMealLogsByDate(today).first()
        val todayWorkouts = workoutLogDao.getWorkoutLogsByDate(today).first()
        val recipesList = recipeDao.getAllRecipes().first()
        val exercisesList = exerciseDao.getAllExercises().first()
        val routineDaysList = routineDayDao.getAllRoutineDays().first()

        val totalCal = todayMeals.sumOf { it.calories }
        val totalProtein = todayMeals.sumOf { it.proteinGrams }
        val totalCarbs = todayMeals.sumOf { it.carbsGrams }
        val totalFat = todayMeals.sumOf { it.fatGrams }
        val totalCostToday = todayMeals.sumOf { it.cost }

        val mealsSummary = if (todayMeals.isEmpty()) {
            "No meals logged today yet."
        } else {
            todayMeals.joinToString("\n") { "- [${it.mealType}] ${it.name}: ${it.calories} kcal, P:${it.proteinGrams}g, C:${it.carbsGrams}g, F:${it.fatGrams}g, Cost: $${String.format(Locale.US, "%.2f", it.cost)}" }
        }

        val workoutSummary = if (todayWorkouts.isEmpty()) {
            "No workout sets logged today yet."
        } else {
            todayWorkouts.groupBy { it.exerciseName }.map { (exName, sets) ->
                val setStr = sets.joinToString(", ") { "Set ${it.setNumber}: ${it.weightKg}kg load x ${it.repsCompleted} reps (${it.difficultyLevel})" }
                "- $exName: $setStr"
            }.joinToString("\n")
        }

        val recipeSummary = if (recipesList.isEmpty()) {
            "None"
        } else {
            recipesList.joinToString("\n") { "- ${it.name} (${if (it.isMassGainerShake) "Mass Shake" else "Meal"}): ${it.totalCalories} kcal, P:${it.totalProtein}g, Cost: $${String.format(Locale.US, "%.2f", it.estimatedCost)}" }
        }

        return """
            Date: $today
            ATHLETE PROFILE & TARGETS:
            - Equipment Status: NON-GYM-EQUIPMENT ONLY (Home Calisthenics & Bodyweight)
            - Current Weight: ${goals.currentWeightKg} kg | Target Weight: ${goals.targetWeightKg} kg
            - Daily Calorie Target: ${goals.targetCalories} kcal (Logged Today: $totalCal kcal, Remaining: ${goals.targetCalories - totalCal} kcal)
            - Target Protein: ${goals.targetProtein}g (Logged: ${totalProtein}g)
            - Total Food/Shake Cost Logged Today: $${String.format(Locale.US, "%.2f", totalCostToday)}
            
            TODAY'S MEAL LOG:
            $mealsSummary
            
            TODAY'S WORKOUT PROGRESSION:
            $workoutSummary
            
            SAVED MASS RECIPES & SHAKES:
            $recipeSummary
            
            SAVED ROUTINE DAYS:
            ${routineDaysList.joinToString(", ") { it.dayName }}
            
            EXERCISE LIBRARY (${exercisesList.size} exercises):
            ${exercisesList.take(8).joinToString(", ") { "${it.name} (${it.difficultyLevel})" }}
        """.trimIndent()
    }
}

