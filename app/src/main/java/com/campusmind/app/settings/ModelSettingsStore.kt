package com.campusmind.app.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.campusmind.app.model.AiCorePreference
import com.campusmind.app.model.AiCoreReleaseStage
import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.modelSettingsDataStore by preferencesDataStore(name = "model_settings")

class ModelSettingsStore(private val context: Context) {
  private val modelPathKey = stringPreferencesKey("model_path")
  private val backendKey = stringPreferencesKey("backend")
  private val maxTokensKey = intPreferencesKey("max_tokens")
  private val temperatureKey = floatPreferencesKey("temperature")
  private val runtimeTypeKey = stringPreferencesKey("runtime_type")
  private val aiCoreReleaseStageKey = stringPreferencesKey("aicore_release_stage")
  private val aiCorePreferenceKey = stringPreferencesKey("aicore_preference")
  private val topKKey = intPreferencesKey("top_k")
  private val maxOutputTokensKey = intPreferencesKey("max_output_tokens")
  private val runtimeStatusTextKey = stringPreferencesKey("runtime_status_text")

  val config: Flow<ModelConfig> =
    context.modelSettingsDataStore.data.map { preferences ->
      ModelConfig(
        modelPath = preferences[modelPathKey].orEmpty(),
        backend = preferences[backendKey] ?: "GPU",
        maxTokens = preferences[maxTokensKey] ?: 512,
        temperature = preferences[temperatureKey] ?: 0.4f,
        runtimeType = preferences[runtimeTypeKey].toEnumOrDefault(RuntimeType.AICORE),
        aiCoreReleaseStage = preferences[aiCoreReleaseStageKey].toEnumOrDefault(AiCoreReleaseStage.STABLE),
        aiCorePreference = preferences[aiCorePreferenceKey].toEnumOrDefault(AiCorePreference.FAST),
        topK = preferences[topKKey] ?: 40,
        maxOutputTokens = preferences[maxOutputTokensKey] ?: 512,
        runtimeStatusText = preferences[runtimeStatusTextKey] ?: "AI Core is the default runtime",
      )
    }

  suspend fun save(config: ModelConfig) {
    context.modelSettingsDataStore.edit { preferences ->
      preferences[modelPathKey] = config.modelPath
      preferences[backendKey] = config.backend
      preferences[maxTokensKey] = config.maxTokens
      preferences[temperatureKey] = config.temperature
      preferences[runtimeTypeKey] = config.runtimeType.name
      preferences[aiCoreReleaseStageKey] = config.aiCoreReleaseStage.name
      preferences[aiCorePreferenceKey] = config.aiCorePreference.name
      preferences[topKKey] = config.topK
      preferences[maxOutputTokensKey] = config.maxOutputTokens
      preferences[runtimeStatusTextKey] = config.runtimeStatusText
    }
  }

  private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
    this?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
