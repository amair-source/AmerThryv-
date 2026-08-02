package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserGoals(
    val targetCalories: Int = 3200,
    val targetProtein: Int = 180,
    val targetCarbs: Int = 400,
    val targetFat: Int = 90,
    val currentWeightKg: Double = 72.5,
    val targetWeightKg: Double = 80.0,
    val customApiKey: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val TARGET_CALORIES = intPreferencesKey("target_calories")
        val TARGET_PROTEIN = intPreferencesKey("target_protein")
        val TARGET_CARBS = intPreferencesKey("target_carbs")
        val TARGET_FAT = intPreferencesKey("target_fat")
        val CURRENT_WEIGHT = doublePreferencesKey("current_weight")
        val TARGET_WEIGHT = doublePreferencesKey("target_weight")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
    }

    val userGoals: Flow<UserGoals> = context.dataStore.data.map { preferences ->
        UserGoals(
            targetCalories = preferences[Keys.TARGET_CALORIES] ?: 3200,
            targetProtein = preferences[Keys.TARGET_PROTEIN] ?: 180,
            targetCarbs = preferences[Keys.TARGET_CARBS] ?: 400,
            targetFat = preferences[Keys.TARGET_FAT] ?: 90,
            currentWeightKg = preferences[Keys.CURRENT_WEIGHT] ?: 72.5,
            targetWeightKg = preferences[Keys.TARGET_WEIGHT] ?: 80.0,
            customApiKey = preferences[Keys.CUSTOM_API_KEY] ?: ""
        )
    }

    suspend fun updateUserGoals(
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        currentWeight: Double,
        targetWeight: Double
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TARGET_CALORIES] = calories
            preferences[Keys.TARGET_PROTEIN] = protein
            preferences[Keys.TARGET_CARBS] = carbs
            preferences[Keys.TARGET_FAT] = fat
            preferences[Keys.CURRENT_WEIGHT] = currentWeight
            preferences[Keys.TARGET_WEIGHT] = targetWeight
        }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CUSTOM_API_KEY] = apiKey.trim()
        }
    }
}
