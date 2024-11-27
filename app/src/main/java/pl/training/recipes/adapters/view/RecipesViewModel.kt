package pl.training.recipes.adapters.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.training.recipes.R
import pl.training.recipes.common.ViewState
import pl.training.recipes.common.ViewState.Failure
import pl.training.recipes.common.ViewState.Initial
import pl.training.recipes.common.ViewState.Processing
import pl.training.recipes.common.ViewState.Success
import pl.training.recipes.domain.RefreshRecipesUseCase
import pl.training.recipes.domain.Recipe
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val refreshRecipesUseCase: RefreshRecipesUseCase
) : ViewModel() {

    private val state = MutableLiveData<ViewState>(Initial)

    val viewState: LiveData<ViewState> = state

    fun refresh() {
        viewModelScope.launch {
            try {
                state.postValue(Processing)
                val data = refreshRecipesUseCase.execute().map(::toView)
                state.postValue(Success(data))
            } catch (_: Exception) {
                state.postValue(Failure(R.string.refresh_recipes_failed))
            }
        }
    }

    private fun toView(recipe: Recipe) = with(recipe) {
        RecipeViewModel(name, cuisine, difficulty)
    }

}