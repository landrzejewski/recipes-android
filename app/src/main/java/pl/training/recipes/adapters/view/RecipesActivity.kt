package pl.training.recipes.adapters.view

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import dagger.hilt.android.AndroidEntryPoint
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
        binding.refreshButton.setOnClickListener { viewModel.refresh()  }

        viewModel.viewState.observe(this, ::update)

        viewModel.loadCached()
    }

    private fun update(viewState: ViewState) = when (viewState) {
        is Initial -> {}
        is Processing -> binding.progressIndicator.visibility = VISIBLE
        is Success<*> -> showRecipes(viewState.get())
        is Failure -> showError(viewState.messageId)
    }

    private fun showRecipes(recipes: List<RecipeViewModel>) {
        binding.progressIndicator.visibility = GONE
        adapter.update(recipes)
    }

    private fun showError(messageId: Int) {
        binding.progressIndicator.visibility = GONE
        Toast.makeText(this, getString(messageId), LENGTH_LONG).show()
    }

}