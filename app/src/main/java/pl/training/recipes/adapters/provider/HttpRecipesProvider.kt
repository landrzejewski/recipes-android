package pl.training.recipes.adapters.provider

import pl.training.recipes.adapters.provider.dto.RecipesDto
import retrofit2.http.GET

interface HttpRecipesProvider {

    @GET("recipes")
    suspend fun get(): RecipesDto

}