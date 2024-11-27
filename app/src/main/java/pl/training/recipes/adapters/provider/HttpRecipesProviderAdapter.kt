package pl.training.recipes.adapters.provider

import pl.training.recipes.domain.RecipesProvider

class HttpRecipesProviderAdapter(
    private val provider: HttpRecipesProvider,
    private val mapper: HttpRecipesProviderMapper
) : RecipesProvider {

    override suspend fun get() = mapper.toDomain(provider.get())

}