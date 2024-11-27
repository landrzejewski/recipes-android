package pl.training.recipes.adapters.provider

import android.util.Log
import pl.training.recipes.domain.RecipesProvider
import pl.training.recipes.domain.RefreshFailedException

class HttpRecipesProviderAdapter(
    private val provider: HttpRecipesProvider,
    private val mapper: HttpRecipesProviderMapper
) : RecipesProvider {

    private val tag = HttpRecipesProviderAdapter::class.java.canonicalName

    override suspend fun get() = try {
        mapper.toDomain(provider.get())
    } catch (exception: Exception) {
        Log.d(tag, "Exception: ${exception.message}")
        throw RefreshFailedException()
    }

}