package pl.training.recipes.adapters.provider.dto

import com.google.gson.annotations.SerializedName

data class RecipesDto(
    @SerializedName("recipes")
    val content: List<RecipeDto>
)