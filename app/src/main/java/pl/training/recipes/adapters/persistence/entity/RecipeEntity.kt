package pl.training.recipes.adapters.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecipeEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val ingredients: String,
    val instructions: String,
    val tags: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val cuisine: String
)