package pl.training.recipes.domain

class RefreshRecipesUseCase(
    private val provider: RecipesProvider,
    private val repository: RecipesRepository
) {

    suspend fun execute(): List<Recipe> {
        val recipes = provider.get()
        repository.save(recipes)
        return recipes
    }

}