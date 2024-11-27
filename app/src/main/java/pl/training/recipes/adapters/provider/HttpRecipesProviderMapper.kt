package pl.training.recipes.adapters.provider

import pl.training.recipes.adapters.provider.dto.RecipesDto
import pl.training.recipes.domain.Recipe

class HttpRecipesProviderMapper {

    fun toDomain(recipesDto: RecipesDto) = recipesDto.content
        .map {
            with(it) {
                Recipe(
                    id,
                    name,
                    ingredients,
                    instructions,
                    tags,
                    prepTimeMinutes,
                    cookTimeMinutes,
                    difficulty,
                    cuisine
                )
            }
        }

}