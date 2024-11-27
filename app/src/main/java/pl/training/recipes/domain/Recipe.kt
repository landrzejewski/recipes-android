package pl.training.recipes.domain

class Recipe(
    val id: Long,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val tags: Set<String>,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val cuisine: String
)