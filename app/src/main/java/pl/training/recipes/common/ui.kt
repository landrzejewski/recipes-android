package pl.training.recipes.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun enableSaveArea(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { mainView, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        mainView.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}