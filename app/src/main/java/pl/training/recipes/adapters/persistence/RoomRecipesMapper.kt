package pl.training.recipes.adapters.persistence

import pl.training.recipes.adapters.persistence.entity.RecipeEntity
import pl.training.recipes.domain.Recipe

class RoomRecipesMapper {

    fun toDomain(recipesEntities: List<RecipeEntity>) = recipesEntities
        .map {
            with(it) {
                Recipe(
                    id,
                    name,
                    toDomain(ingredients),
                    toDomain(instructions),
                    toDomain(tags).toSet(),
                    prepTimeMinutes,
                    cookTimeMinutes,
                    difficulty,
                    cuisine
                )
            }
        }

    fun toEntity(recipes: List<Recipe>) = recipes
        .map {
            with(it) {
                RecipeEntity(
                    id,
                    name,
                    toEntity(ingredients),
                    toEntity(instructions),
                    toEntity(tags.toList()),
                    prepTimeMinutes,
                    cookTimeMinutes,
                    difficulty,
                    cuisine
                )
            }
        }

    private fun toDomain(text: String) = text.split(SEPARATOR)

    private fun toEntity(values: List<String>) = values.joinToString(SEPARATOR)

    private companion object {

        private const val SEPARATOR = ";"

    }

}