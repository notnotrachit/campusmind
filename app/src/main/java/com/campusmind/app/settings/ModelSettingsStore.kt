package com.campusmind.app.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.campusmind.app.model.CAMPUS_MODEL_COMMIT
import com.campusmind.app.model.CAMPUS_MODEL_FILE
import com.campusmind.app.model.CAMPUS_MODEL_ID
import com.campusmind.app.model.CAMPUS_MODEL_MAX_TOKENS
import com.campusmind.app.model.CAMPUS_MODEL_NAME
import com.campusmind.app.model.CAMPUS_MODEL_SIZE_BYTES
import com.campusmind.app.model.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.modelSettingsDataStore by preferencesDataStore(name = "model_settings")
private const val MIN_MODEL_TOKENS = 1

class ModelSettingsStore(private val context: Context) {
  private val modelPathKey = stringPreferencesKey("model_path")
  private val modelNameKey = stringPreferencesKey("model_name")
  private val modelIdKey = stringPreferencesKey("model_id")
  private val modelFileKey = stringPreferencesKey("model_file")
  private val commitHashKey = stringPreferencesKey("commit_hash")
  private val sizeInBytesKey = longPreferencesKey("size_in_bytes")
  private val backendKey = stringPreferencesKey("backend")
  private val maxTokensKey = intPreferencesKey("max_tokens")
  private val temperatureKey = floatPreferencesKey("temperature")
  private val topKKey = intPreferencesKey("top_k")
  private val topPKey = floatPreferencesKey("top_p")

  val config: Flow<ModelConfig> =
    context.modelSettingsDataStore.data.map { preferences ->
      val savedPath = preferences[modelPathKey].orEmpty()
      ModelConfig(
        modelPath = savedPath.takeIf { it.endsWith(CAMPUS_MODEL_FILE) }.orEmpty(),
        modelName = CAMPUS_MODEL_NAME,
        modelId = CAMPUS_MODEL_ID,
        modelFile = CAMPUS_MODEL_FILE,
        commitHash = CAMPUS_MODEL_COMMIT,
        sizeInBytes = CAMPUS_MODEL_SIZE_BYTES,
        backend = preferences[backendKey] ?: "CPU",
        maxTokens = (preferences[maxTokensKey] ?: CAMPUS_MODEL_MAX_TOKENS).coerceIn(MIN_MODEL_TOKENS, CAMPUS_MODEL_MAX_TOKENS),
        temperature = (preferences[temperatureKey] ?: 0.4f).coerceIn(0f, 1f),
        topK = (preferences[topKKey] ?: 40).coerceIn(1, 40),
        topP = preferences[topPKey] ?: 0.95f,
      )
    }

  suspend fun save(config: ModelConfig) {
    context.modelSettingsDataStore.edit { preferences ->
      preferences[modelPathKey] = config.modelPath
      preferences[modelNameKey] = config.modelName
      preferences[modelIdKey] = config.modelId
      preferences[modelFileKey] = config.modelFile
      preferences[commitHashKey] = config.commitHash
      preferences[sizeInBytesKey] = config.sizeInBytes
      preferences[backendKey] = config.backend
      preferences[maxTokensKey] = config.maxTokens.coerceIn(MIN_MODEL_TOKENS, CAMPUS_MODEL_MAX_TOKENS)
      preferences[temperatureKey] = config.temperature.coerceIn(0f, 1f)
      preferences[topKKey] = config.topK.coerceIn(1, 40)
      preferences[topPKey] = config.topP.coerceIn(0f, 1f)
    }
  }
}
