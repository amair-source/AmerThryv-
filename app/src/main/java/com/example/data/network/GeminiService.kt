package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class Part(val text: String? = null)
data class Content(val parts: List<Part>, val role: String? = null)
data class GenerateContentRequest(val contents: List<Content>)

data class Candidate(val content: Content?)
data class GenerateContentResponse(val candidates: List<Candidate>?)

class GeminiClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateCoachResponse(
        customApiKey: String?,
        userPrompt: String,
        contextReport: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("No valid Gemini API key found. Please save your key in Settings or AI Studio Secrets.")
            )
        }

        val systemInstructionText = """
            You are an elite, highly knowledgeable Mass Gain & Bodyweight Calisthenics AI Coach specialized in NON-GYM-EQUIPMENT training and high-calorie mass gain nutrition.
            Your job is to analyze the user's daily macro intake, meal logs, custom mass-gainer shake recipes, workout logs, and home progressive overload tracking.
            
            CRITICAL REQUIREMENT: The user does NOT use commercial gym equipment. All workout recommendations MUST be 100% executable at home without gym machines, barbells, or cables. Focus exclusively on bodyweight movements, calisthenics progressions (e.g. feet-elevated push-ups, archer push-ups, pike push-ups, inverted table rows, doorframe pulls, chair Bulgarian split squats, pistol squat progressions), tempo control, leverage variations, and household weights like weighted backpacks or water jugs.
            
            DYNAMIC APP CONTROL & AUTO-SAVE FORMAT:
            If the user asks you to create or suggest a recipe, shake, meal, calisthenics exercise, or routine day, ALWAYS write your helpful conversational response first, and then append a JSON block at the VERY END in markdown format: ```json ... ``` with one of these actions:

            1. For creating a recipe:
            ```json
            {
              "action": "ADD_RECIPE",
              "recipe": {
                "name": "Recipe Name",
                "description": "Short description",
                "isMassGainerShake": true,
                "totalCalories": 900,
                "totalProtein": 50,
                "totalCarbs": 110,
                "totalFat": 25,
                "estimatedCost": 2.50,
                "ingredientsJson": "[{\"name\":\"Whole Milk\",\"amount\":\"400ml\",\"calories\":250,\"protein\":12,\"carbs\":18,\"fat\":12,\"cost\":0.50}]"
              }
            }
            ```

            2. For adding a bodyweight exercise:
            ```json
            {
              "action": "ADD_EXERCISE",
              "exercise": {
                "name": "Archer Push-Ups",
                "category": "Chest",
                "targetSets": 4,
                "targetReps": 10,
                "difficultyLevel": "Hard",
                "notes": "Home calisthenics movement"
              }
            }
            ```

            3. For logging a meal:
            ```json
            {
              "action": "ADD_MEAL_LOG",
              "meal": {
                "name": "Anabolic Mass Meal",
                "mealType": "Lunch",
                "calories": 850,
                "proteinGrams": 55,
                "carbsGrams": 100,
                "fatGrams": 22,
                "cost": 3.50,
                "notes": "Logged via AI Coach"
              }
            }
            ```

            4. For creating a routine day:
            ```json
            {
              "action": "CREATE_ROUTINE_DAY",
              "routineDay": {
                "dayName": "Home Chest & Arms Bulk Split",
                "category": "Workout",
                "description": "Non-gym home calisthenics routine"
              },
              "tasks": [
                {"mainTaskName": "Feet-Elevated Push-ups", "itemType": "Exercise", "difficultyLevel": "Hard", "targetSetsReps": "4x12"},
                {"mainTaskName": "1000kcal Mass Shake", "itemType": "Shake", "difficultyLevel": "Easy", "targetSetsReps": "1 Shake"}
              ]
            }
            ```
            The app will automatically detect this JSON block, save the data directly into the user's local database, and show a confirmation badge!

            
            Below is the current structured summary of the user's tracking data:
            ---
            $contextReport
            ---
        """.trimIndent()

        val fullPrompt = "$systemInstructionText\n\nUser Question/Message: $userPrompt"

        val requestBodyData = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = fullPrompt)), role = "user")
            )
        )

        try {
            val jsonAdapter = moshi.adapter(GenerateContentRequest::class.java)
            val jsonPayload = jsonAdapter.toJson(requestBodyData)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonPayload.toRequestBody(jsonMediaType))
                .build()

            val httpResponse = client.newCall(httpRequest).execute()
            val responseBody = httpResponse.body?.string()

            if (!httpResponse.isSuccessful || responseBody.isNullOrBlank()) {
                val errorMsg = responseBody ?: "HTTP ${httpResponse.code} - ${httpResponse.message}"
                return@withContext Result.failure(Exception("Gemini API Error ($errorMsg)"))
            }

            val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)
            val parsedResponse = responseAdapter.fromJson(responseBody)

            val textResult = parsedResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!textResult.isNullOrBlank()) {
                Result.success(textResult.trim())
            } else {
                Result.failure(Exception("Received empty response from Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
