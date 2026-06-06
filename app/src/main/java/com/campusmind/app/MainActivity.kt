package com.campusmind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusmind.app.ui.CampusMindApp
import com.campusmind.app.ui.CampusMindViewModel
import com.campusmind.app.ui.CampusMindViewModelFactory
import com.campusmind.app.ui.CampusTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val app = application as CampusMindApplication
    setContent {
      CampusTheme {
        val viewModel: CampusMindViewModel = viewModel(
          factory = CampusMindViewModelFactory(
            repository = app.repository,
            modelSettingsStore = app.modelSettingsStore,
            modelDownloadManager = app.modelDownloadManager,
          ),
        )
        CampusMindApp(viewModel = viewModel)
      }
    }
  }
}
