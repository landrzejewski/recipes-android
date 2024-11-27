package pl.training.recipes.adapters.view

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.training.recipes.R
import pl.training.recipes.common.enableSaveArea
import pl.training.recipes.databinding.ActivityRecipesBinding

class RecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        enableSaveArea(binding.main)


        binding.helloText.text = "text"
    }

}