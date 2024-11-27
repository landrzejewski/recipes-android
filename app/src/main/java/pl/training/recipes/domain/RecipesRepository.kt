package pl.training.recipes.domain

interface RecipesRepository {

    suspend fun save(recipes: List<Recipe>)

    suspend fun findAll(): List<Recipe>

}