package pl.training.recipes.adapters.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.training.recipes.R
import pl.training.recipes.databinding.ItemRecipieBinding

class RecipesViewAdapter(private var recipes: List<RecipeViewModel> = emptyList()) :
    RecyclerView.Adapter<RecipesViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemRecipieBinding.bind(view)

        fun update(recipe: RecipeViewModel) {
            binding.name.text = recipe.name
            binding.cuisine.text = recipe.cuisine
            binding.difficulty.text = recipe.difficulty
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_recipie, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = recipes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.update(recipes[position])

    @SuppressLint("NotifyDataSetChanged")
    fun update(recipes: List<RecipeViewModel>) {
        this.recipes = recipes
        notifyDataSetChanged()
    }

}