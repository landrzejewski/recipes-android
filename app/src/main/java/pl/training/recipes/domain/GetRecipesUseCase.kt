package pl.training.recipes.domain

class GetRecipesUseCase(private val recipesProvider: RecipesProvider) {

    suspend fun execute(): List<Recipe> = recipesProvider.get()
        .take(20)
        .shuffled()

}