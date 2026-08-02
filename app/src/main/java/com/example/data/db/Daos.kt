package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY id DESC")
    fun getMealLogsByDate(date: String): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs ORDER BY date DESC, id DESC LIMIT 100")
    fun getAllRecentMealLogs(): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Update
    suspend fun updateMealLog(mealLog: MealLog)

    @Delete
    suspend fun deleteMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLogById(id: Long)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isMassGainerShake = 1 ORDER BY id DESC")
    fun getMassGainerShakes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: Long)
}

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY exerciseName ASC, setNumber ASC")
    fun getWorkoutLogsByDate(date: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE exerciseId = :exerciseId ORDER BY date DESC, setNumber ASC")
    fun getLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs ORDER BY date DESC, id DESC LIMIT 200")
    fun getAllRecentWorkoutLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog)

    @Update
    suspend fun updateWorkoutLog(log: WorkoutLog)

    @Delete
    suspend fun deleteWorkoutLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutLogById(id: Long)
}

@Dao
interface RoutineDayDao {
    @Query("SELECT * FROM routine_days ORDER BY id ASC")
    fun getAllRoutineDays(): Flow<List<RoutineDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineDay(day: RoutineDay): Long

    @Update
    suspend fun updateRoutineDay(day: RoutineDay)

    @Query("DELETE FROM routine_days WHERE id = :id")
    suspend fun deleteRoutineDayById(id: Long)
}

@Dao
interface RoutineTaskDao {
    @Query("SELECT * FROM routine_tasks WHERE dayId = :dayId ORDER BY id ASC")
    fun getTasksForDay(dayId: Long): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks ORDER BY id ASC")
    fun getAllRoutineTasks(): Flow<List<RoutineTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: RoutineTask): Long

    @Update
    suspend fun updateTask(task: RoutineTask)

    @Query("DELETE FROM routine_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementRecord)

    @Update
    suspend fun updateAchievement(achievement: AchievementRecord)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

