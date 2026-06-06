package com.campusmind.app.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.campusmind.app.model.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.modelSettingsDataStore by preferencesDataStore(name = "model_settings")

class ModelSettingsStore(private val context: Context) {
  private val modelPathKey = stringPreferencesKey("model_path")
  private val backendKey = stringPreferencesKey("backend")
  private val maxTokensKey = intPreferencesKey("max_tokens")
  private val temperatureKey = floatPreferencesKey("temperature")

  val config: Flow<ModelConfig> =
    context.modelSettingsDataStore.data.map { preferences ->
      ModelConfig(
        modelPath = preferences[modelPathKey].orEmpty(),
        backend = preferences[backendKey] ?: "GPU",
        maxTokens = preferences[maxTokensKey] ?: 512,
        temperature = preferences[temperatureKey] ?: 0.4f,
      )
    }

  suspend fun save(config: ModelConfig) {
    context.modelSettingsDataStore.edit { preferences ->
      preferences[modelPathKey] = config.modelPath
      preferences[backendKey] = config.backend
      preferences[maxTokensKey] = config.maxTokens
      preferences[temperatureKey] = config.temperature
    }
  }
}
