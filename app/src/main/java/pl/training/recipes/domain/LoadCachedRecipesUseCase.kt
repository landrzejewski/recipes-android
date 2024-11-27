package pl.training.recipes.domain

class LoadCachedRecipesUseCase(
    private val repository: RecipesRepository
) {

    suspend fun execute() = repository.findAll()

}