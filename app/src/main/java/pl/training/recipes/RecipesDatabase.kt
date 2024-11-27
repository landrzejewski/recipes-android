package pl.training.recipes

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.training.recipes.adapters.persistence.RecipesDao
import pl.training.recipes.adapters.persistence.entity.RecipeEntity

@Database(
    entities = [RecipeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RecipesDatabase : RoomDatabase() {

    abstract fun recipesDao(): RecipesDao

}