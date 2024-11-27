package pl.training.recipes.adapters.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.training.recipes.adapters.persistence.entity.RecipeEntity

@Dao
interface RecipesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(recipes: List<RecipeEntity>)

    @Query("select * from RecipeEntity")
    suspend fun findAll(): List<RecipeEntity>

}