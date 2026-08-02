package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack", "Mass Shake"
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val cost: Double = 0.0, // Recipe/Ingredient cost tracking
    val notes: String = ""
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val isMassGainerShake: Boolean = false,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val estimatedCost: Double = 0.0,
    val ingredientsJson: String = "[]" // JSON string representing ingredients list with cost per item
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Chest", "Legs", "Back", "Shoulders", "Arms", "Core"
    val targetSets: Int = 4,
    val targetReps: Int = 12,
    val difficultyLevel: String = "Medium", // "Beginner", "Medium", "Hard", "Weighted Backpack"
    val notes: String = ""
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val weightKg: Double = 0.0, // Additional load/backpack weight
    val repsCompleted: Int,
    val difficultyLevel: String = "Medium",
    val isPersonalRecord: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "routine_days")
data class RoutineDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayName: String, // e.g., "Chest & Triceps Home Split", "Legs & Core Calisthenics", "Bulk Day 1"
    val category: String = "Workout", // "Workout", "Nutrition", "Bulk Day"
    val description: String = ""
)

@Entity(tableName = "routine_tasks")
data class RoutineTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val mainTaskName: String, // e.g., "Feet-Elevated Push-Ups", "Morning 1000kcal Shake"
    val itemType: String = "Exercise", // "Exercise", "Meal", "General"
    val difficultyLevel: String = "Medium",
    val targetSetsReps: String = "4x12",
    val subtasksJson: String = "[]", // JSON string list of subtasks
    val isCompleted: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementRecord(
    @PrimaryKey val id: String, // e.g. "FIRST_2KG", "LOG_7_DAYS", "CALORIES_3_DAYS", "WORKOUT_10", "RECIPE_MASTER"
    val title: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val messageText: String,
    val isContextGenerated: Boolean = false,
    val autoAppliedAction: String = ""
)

