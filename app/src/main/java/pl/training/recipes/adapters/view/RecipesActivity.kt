package pl.training.recipes.adapters.view

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import dagger.hilt.android.AndroidEntryPoint
import pl.training.recipes.R
import pl.training.recipes.common.ViewState
import pl.training.recipes.common.ViewState.Failure
import pl.training.recipes.common.ViewState.Initial
import pl.training.recipes.common.ViewState.Processing
import pl.training.recipes.common.ViewState.Success
import pl.training.recipes.common.enableSaveArea
import pl.training.recipes.databinding.ActivityRecipesBinding

@AndroidEntryPoint
class RecipesActivity : AppCompatActivity() {

    private val viewModel: RecipesViewModel by viewModels()
    private val adapter = RecipesViewAdapter()
    private lateinit var binding: ActivityRecipesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        enableSaveArea(binding.main)

        binding.recipes.layoutManager = LinearLayoutManager(this, VERTICAL, false)
        binding.recipes.adapter = adapter
        viewModel.viewState.observe(this, ::update)

        viewModel.refresh()
    }

    private fun update(viewState: ViewState) = when (viewState) {
        is Initial -> {}
        is Processing -> {}
        is Success<*> -> showRecipes(viewState.get())
        is Failure -> Toast.makeText(this, getString(viewState.messageId), LENGTH_LONG).show()
    }

    private fun showRecipes(recipes: List<RecipeViewModel>) {
        adapter.update(recipes)
    }

}