package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.Recipe
import com.example.ui.theme.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class IngredientItem(
    val name: String,
    val amount: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val cost: Double = 0.0
)

@Composable
fun RecipesScreen(
    recipes: List<Recipe>,
    onLogRecipeAsMeal: (Recipe) -> Unit,
    onAddRecipeClick: () -> Unit,
    onDeleteRecipe: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUSTOM RECIPES & SHAKES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MassOrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "High-Calorie Mass Lab",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onAddRecipeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_recipe_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recipe", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Recipe", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (recipes.isEmpty()) {
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
                            imageVector = Icons.Default.Blender,
                            contentDescription = "No Recipes",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No custom recipes created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Build high-calorie mass gainers, ingredient cost breakdowns, or protein bowls!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onLogAsMeal = { onLogRecipeAsMeal(recipe) },
                    onDelete = { onDeleteRecipe(recipe.id) }
                )
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onLogAsMeal: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val ingredientsList = remember(recipe.ingredientsJson) {
        parseIngredientsJson(recipe.ingredientsJson)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (recipe.isMassGainerShake) MassOrangePrimary.copy(alpha = 0.6f) else DarkBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (recipe.isMassGainerShake) {
                        Surface(
                            color = MassOrangePrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "MASS GAINER SHAKE 🔥",
                                style = MaterialTheme.typography.labelSmall,
                                color = MassOrangePrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (recipe.description.isNotBlank()) {
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Recipe",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Macro summary bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "${recipe.totalCalories} kcal", style = MaterialTheme.typography.bodyLarge, color = MacroCaloriesColor, fontWeight = FontWeight.Bold)
                    Text(text = "P: ${recipe.totalProtein}g", style = MaterialTheme.typography.bodyMedium, color = MacroProteinColor, fontWeight = FontWeight.Bold)
                    Text(text = "C: ${recipe.totalCarbs}g", style = MaterialTheme.typography.bodyMedium, color = MacroCarbsColor)
                    Text(text = "F: ${recipe.totalFat}g", style = MaterialTheme.typography.bodyMedium, color = MacroFatColor)
                }

                if (recipe.estimatedCost > 0) {
                    Text(
                        text = "~$${String.format(java.util.Locale.US, "%.2f", recipe.estimatedCost)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            // Ingredient toggle button
            if (ingredientsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Ingredients",
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) "Hide Ingredients (${ingredientsList.size})" else "View Ingredients (${ingredientsList.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }

                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ingredientsList.forEach { ing ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${ing.name} (${ing.amount})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${ing.calories} kcal | P:${ing.protein}g C:${ing.carbs}g F:${ing.fat}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button to Log as Meal
            Button(
                onClick = onLogAsMeal,
                colors = ButtonDefaults.buttonColors(containerColor = MassOrangePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log as Meal Today (+${recipe.totalCalories} kcal)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun parseIngredientsJson(json: String): List<IngredientItem> {
    if (json.isBlank() || json == "[]") return emptyList()
    return try {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, IngredientItem::class.java)
        val adapter = moshi.adapter<List<IngredientItem>>(type)
        adapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
