package pl.training.recipes.adapters.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import pl.training.recipes.R
import pl.training.recipes.common.ViewState
import pl.training.recipes.common.ViewState.Failure
import pl.training.recipes.common.ViewState.Initial
import pl.training.recipes.common.ViewState.Processing
import pl.training.recipes.common.ViewState.Success
import pl.training.recipes.domain.LoadCachedRecipesUseCase
import pl.training.recipes.domain.RefreshRecipesUseCase
import pl.training.recipes.domain.Recipe
import pl.training.recipes.domain.RefreshFailedException
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val refreshRecipesUseCase: RefreshRecipesUseCase,
    private val loadCachedRecipesUseCase: LoadCachedRecipesUseCase
) : ViewModel() {

    private val state = MutableLiveData<ViewState>(Initial)
    private var job: Job? = null

    val viewState: LiveData<ViewState> = state

    fun loadCached() {
        viewModelScope.launch {
            state.postValue(Processing)
            val data = loadCachedRecipesUseCase.execute().map(::toView)
            state.postValue(Success(data))
        }
    }

    fun refresh() {
        job = viewModelScope.launch {
            try {
                state.postValue(Processing)
                //withTimeout(3_000) {
                    delay(5_000)
                    val data = refreshRecipesUseCase.execute().map(::toView)
                    state.postValue(Success(data))
                //}
            } catch (c: CancellationException) {
                state.postValue(Success(emptyList<RecipeViewModel>()))
            } catch (refreshFailed: RefreshFailedException) {
                state.postValue(Failure(R.string.refresh_recipes_failed))
            }
        }

    }

    fun cancel() {
        job?.cancel()
    }

    private fun toView(recipe: Recipe) = with(recipe) {
        RecipeViewModel(name, cuisine, difficulty)
    }

}