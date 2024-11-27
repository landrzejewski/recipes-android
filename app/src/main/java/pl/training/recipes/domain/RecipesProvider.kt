package pl.training.recipes.domain

interface RecipesProvider {

    suspend fun get(): List<Recipe>

}