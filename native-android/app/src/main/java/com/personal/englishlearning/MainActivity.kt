package com.personal.englishlearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.englishlearning.ui.EnglishLearningApp
import com.personal.englishlearning.ui.MainViewModel
import com.personal.englishlearning.ui.MainViewModelFactory
import com.personal.englishlearning.ui.theme.EnglishLearningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as EnglishLearningApplication).container
        setContent {
            EnglishLearningTheme {
                val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(container))
                EnglishLearningApp(viewModel)
            }
        }
    }
}
