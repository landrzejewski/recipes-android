package pl.training.recipes.adapters.persistence

import pl.training.recipes.domain.Recipe
import pl.training.recipes.domain.RecipesRepository

class RoomRecipesAdapter(
    private val dao: RecipesDao,
    private val mapper: RoomRecipesMapper
) : RecipesRepository {

    override suspend fun save(recipes: List<Recipe>) = dao.save(mapper.toEntity(recipes))

    override suspend fun findAll() = mapper.toDomain(dao.findAll())

}