package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MealLog::class,
        Recipe::class,
        Exercise::class,
        WorkoutLog::class,
        RoutineDay::class,
        RoutineTask::class,
        AchievementRecord::class,
        ChatMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealLogDao(): MealLogDao
    abstract fun recipeDao(): RecipeDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun routineDayDao(): RoutineDayDao
    abstract fun routineTaskDao(): RoutineTaskDao
    abstract fun achievementDao(): AchievementDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mass_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // Populate default Exercises (Non-Gym Calisthenics)
                val exerciseDao = database.exerciseDao()
                val defaultExercises = listOf(
                    Exercise(name = "Feet-Elevated Push-ups", category = "Chest", targetSets = 4, targetReps = 12, difficultyLevel = "Hard", notes = "Elevate feet on couch/chair to target upper chest"),
                    Exercise(name = "Pike Push-ups (Deltoid Focus)", category = "Shoulders", targetSets = 4, targetReps = 10, difficultyLevel = "Medium", notes = "Pike position to target shoulders with bodyweight angle"),
                    Exercise(name = "Inverted Table / Chair Rows", category = "Back", targetSets = 4, targetReps = 10, difficultyLevel = "Hard", notes = "Pull under sturdy table or broomstick across chairs"),
                    Exercise(name = "Doorframe / Towel Pulls", category = "Back", targetSets = 4, targetReps = 12, difficultyLevel = "Medium", notes = "Focus on back squeeze against sturdy doorframe"),
                    Exercise(name = "Chair Bulgarian Split Squats", category = "Legs", targetSets = 4, targetReps = 12, difficultyLevel = "Hard", notes = "Single-leg hypertrophy builder using home chair"),
                    Exercise(name = "Single-Leg Staircase Calf Raises", category = "Legs", targetSets = 4, targetReps = 20, difficultyLevel = "Medium", notes = "Full stretch off staircase or doorstep"),
                    Exercise(name = "Chair Edge Tricep Dips", category = "Arms", targetSets = 4, targetReps = 15, difficultyLevel = "Medium", notes = "Dips off chair or sofa edge for tricep overload"),
                    Exercise(name = "Weighted Backpack Bicep Curls", category = "Arms", targetSets = 4, targetReps = 15, difficultyLevel = "Weighted Backpack", notes = "Fill backpack with books or water bottles")
                )
                defaultExercises.forEach { exerciseDao.insertExercise(it) }

                // Populate default High-Calorie Mass-Gainer Shake & Meals
                val recipeDao = database.recipeDao()
                val defaultRecipes = listOf(
                    Recipe(
                        name = "Ultra Mass Gainer Shake (1100 kcal)",
                        description = "High-calorie daily mass gainer shake with oats, peanut butter, whole milk, whey, and banana.",
                        isMassGainerShake = true,
                        totalCalories = 1080,
                        totalProtein = 62,
                        totalCarbs = 125,
                        totalFat = 38,
                        estimatedCost = 2.80,
                        ingredientsJson = """[
                            {"name":"Whole Milk","amount":"500 ml","calories":300,"protein":16,"carbs":24,"fat":16,"cost":0.60},
                            {"name":"Rolled Oats","amount":"100 g","calories":380,"protein":13,"carbs":67,"fat":7,"cost":0.40},
                            {"name":"Peanut Butter","amount":"40 g (2 tbsp)","calories":240,"protein":10,"carbs":8,"fat":20,"cost":0.50},
                            {"name":"Whey Protein Powder","amount":"1 scoop (30g)","calories":120,"protein":24,"carbs":2,"fat":1.5,"cost":1.00},
                            {"name":"Large Banana","amount":"1 whole","calories":110,"protein":1.5,"carbs":28,"fat":0.3,"cost":0.30},
                            {"name":"Honey","amount":"1 tbsp","calories":60,"protein":0,"carbs":17,"fat":0,"cost":0.20}
                        ]""".trimIndent()
                    ),
                    Recipe(
                        name = "Anabolic Chicken Rice & Avocado Bowl",
                        description = "Clean high-carbohydrate mass meal with seasoned chicken breast, jasmine rice, and healthy fats.",
                        isMassGainerShake = false,
                        totalCalories = 820,
                        totalProtein = 55,
                        totalCarbs = 95,
                        totalFat = 22,
                        estimatedCost = 3.90,
                        ingredientsJson = """[
                            {"name":"Chicken Breast","amount":"220 g","calories":360,"protein":50,"carbs":0,"fat":8,"cost":2.20},
                            {"name":"Jasmine Rice (Cooked)","amount":"300 g","calories":390,"protein":7,"carbs":84,"fat":1,"cost":0.80},
                            {"name":"Avocado","amount":"1/2 whole","calories":120,"protein":1.5,"carbs":6,"fat":11,"cost":0.90}
                        ]""".trimIndent()
                    )
                )
                defaultRecipes.forEach { recipeDao.insertRecipe(it) }

                // Populate Default Routine Days & Tasks
                val dayDao = database.routineDayDao()
                val taskDao = database.routineTaskDao()

                val day1Id = dayDao.insertRoutineDay(
                    RoutineDay(dayName = "Upper Body & Calisthenics Bulk", category = "Workout", description = "Non-gym home chest, shoulders, back & arms overload split")
                )
                taskDao.insertTask(
                    RoutineTask(
                        dayId = day1Id,
                        mainTaskName = "Morning 1000kcal Mass Gainer Shake",
                        itemType = "Shake",
                        difficultyLevel = "Easy",
                        targetSetsReps = "1 Large Shake",
                        subtasksJson = """["Blend 500ml Whole Milk", "Add 100g Oats & 2 tbsp Peanut Butter", "Add 1 Banana & Scoop Whey"]"""
                    )
                )
                taskDao.insertTask(
                    RoutineTask(
                        dayId = day1Id,
                        mainTaskName = "Feet-Elevated Push-Ups",
                        itemType = "Exercise",
                        difficultyLevel = "Hard",
                        targetSetsReps = "4 Sets x 12 Reps",
                        subtasksJson = """["Elevate feet on bed or chair", "3-second slow lowering tempo", "Explosive push to full extension"]"""
                    )
                )
                taskDao.insertTask(
                    RoutineTask(
                        dayId = day1Id,
                        mainTaskName = "Inverted Table / Chair Rows",
                        itemType = "Exercise",
                        difficultyLevel = "Hard",
                        targetSetsReps = "4 Sets x 10 Reps",
                        subtasksJson = """["Under-table row grip", "Pull chest to table rim", "Squeeze shoulder blades 1 second"]"""
                    )
                )

                val day2Id = dayDao.insertRoutineDay(
                    RoutineDay(dayName = "Lower Body & Core Home Day", category = "Workout", description = "Home single-leg hypertrophy & core conditioning")
                )
                taskDao.insertTask(
                    RoutineTask(
                        dayId = day2Id,
                        mainTaskName = "Chair Bulgarian Split Squats",
                        itemType = "Exercise",
                        difficultyLevel = "Hard",
                        targetSetsReps = "4 Sets x 12 Reps / Leg",
                        subtasksJson = """["Back foot on chair edge", "Maintain vertical torso", "Drive through front heel"]"""
                    )
                )

                // Populate Gamified Achievements
                val achievementDao = database.achievementDao()
                val defaultAchievements = listOf(
                    AchievementRecord(
                        id = "FIRST_LOG",
                        title = "First Step into Mass",
                        description = "Logged your first meal or workout in Mass Pro",
                        category = "Logging",
                        isUnlocked = true,
                        unlockedDate = "Today"
                    ),
                    AchievementRecord(
                        id = "FIRST_2KG_GAIN",
                        title = "First 2kg Mass Milestone",
                        description = "Gained your first 2kg towards your target bodyweight",
                        category = "Mass Gain",
                        isUnlocked = false,
                        unlockedDate = ""
                    ),
                    AchievementRecord(
                        id = "STREAK_7_DAYS",
                        title = "Consistent Logger (7 Days)",
                        description = "Logged nutrition or workouts 7 days in a row",
                        category = "Consistency",
                        isUnlocked = false,
                        unlockedDate = ""
                    ),
                    AchievementRecord(
                        id = "CALORIES_3_DAYS",
                        title = "Surplus Master (Hit Calorie Goal 3 Days)",
                        description = "Hit target calorie surplus 3 days in a row",
                        category = "Nutrition",
                        isUnlocked = false,
                        unlockedDate = ""
                    ),
                    AchievementRecord(
                        id = "WORKOUT_10",
                        title = "Home Calisthenics Hero (10 Workouts)",
                        description = "Completed 10 home bodyweight workout sessions",
                        category = "Workouts",
                        isUnlocked = false,
                        unlockedDate = ""
                    ),
                    AchievementRecord(
                        id = "RECIPE_MASTER",
                        title = "Budget Mass Chef (3 Custom Recipes)",
                        description = "Created 3 custom high-calorie recipes or mass gain shakes",
                        category = "Nutrition",
                        isUnlocked = true,
                        unlockedDate = "Today"
                    )
                )
                defaultAchievements.forEach { achievementDao.insertAchievement(it) }
            }
        }
    }
}

