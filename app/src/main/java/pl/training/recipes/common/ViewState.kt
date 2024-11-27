package pl.training.recipes.common

sealed class ViewState {

    data object Initial : ViewState()
    data object Processing : ViewState()
    class Success<D>(val data: D) : ViewState() {

        @Suppress("UNCHECKED_CAST")
        fun <T> get(): T {
            return data as T
        }

    }
    class Failure(val messageId: Int) : ViewState()

}