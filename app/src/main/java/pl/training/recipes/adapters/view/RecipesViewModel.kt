package pl.training.recipes.adapters.view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.training.recipes.domain.GetRecipesUseCase
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase
): ViewModel() {

     fun refresh() {
        viewModelScope.launch {
            val result = getRecipesUseCase.execute()
            result.forEach{
                Log.d("###", "$it")
            }
        }
    }

}