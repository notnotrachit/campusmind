package com.campusmind.app.ai

import com.campusmind.app.model.AiCorePreference
import com.campusmind.app.model.AiCoreReleaseStage
import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.google.mlkit.genai.prompt.generationConfig
import com.google.mlkit.genai.prompt.modelConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiCoreLocalLlmEngine : LocalLlmEngine {
  override val runtimeType: RuntimeType = RuntimeType.AICORE
  override var isReady: Boolean = false
    private set

  private var generativeModel: GenerativeModel? = null
  private val mutableStatus = MutableStateFlow("AI Core not initialized")
  val status: StateFlow<String> = mutableStatus.asStateFlow()

  override suspend fun initialize(config: ModelConfig): Result<Unit> {
    isReady = false
    val model = Generation.getClient(generationConfig { modelConfig = config.toAiCoreModelConfig() })
    generativeModel = model

    return runCatching {
      when (val status = model.checkStatus()) {
        FeatureStatus.AVAILABLE -> {
          mutableStatus.value = "AI Core available; warming up"
          model.warmup()
          isReady = true
          mutableStatus.value = "AI Core active"
        }
        FeatureStatus.DOWNLOADABLE,
        FeatureStatus.DOWNLOADING -> {
          mutableStatus.value = "AI Core support downloading"
          model.download().collect { downloadStatus ->
            when (downloadStatus) {
              is DownloadStatus.DownloadStarted -> {
                mutableStatus.value = "AI Core downloading ${downloadStatus.bytesToDownload} bytes"
              }
              is DownloadStatus.DownloadProgress -> {
                mutableStatus.value = "AI Core downloaded ${downloadStatus.totalBytesDownloaded} bytes"
              }
              is DownloadStatus.DownloadFailed -> {
                throw downloadStatus.e
              }
              is DownloadStatus.DownloadCompleted -> {
                mutableStatus.value = "AI Core download complete; warming up"
                model.warmup()
                isReady = true
                mutableStatus.value = "AI Core active"
              }
            }
          }
          check(isReady) { "AI Core download did not complete" }
        }
        FeatureStatus.UNAVAILABLE -> {
          throw IllegalStateException("AI Core is unavailable on this device")
        }
        else -> {
          throw IllegalStateException("Unknown AI Core status: $status")
        }
      }
    }.onFailure { error ->
      isReady = false
      mutableStatus.value = "AI Core unavailable: ${error.message ?: error::class.java.simpleName}"
    }
  }

  override suspend fun generate(prompt: String, options: GenerationOptions): Result<String> =
    runCatching {
      val model = checkNotNull(generativeModel) { "AI Core has not been initialized" }
      val request = generateContentRequest(TextPart(prompt)) {
        temperature = options.temperature.coerceIn(0f, 1f)
        topK = options.topK
        maxOutputTokens = options.maxTokens
      }
      val response = StringBuilder()
      model.generateContentStream(request).collect { chunk ->
        response.append(chunk.candidates.firstOrNull()?.text.orEmpty())
      }
      response.toString()
    }

  private fun ModelConfig.toAiCoreModelConfig() = modelConfig {
    releaseStage =
      when (aiCoreReleaseStage) {
        AiCoreReleaseStage.STABLE -> ModelReleaseStage.STABLE
        AiCoreReleaseStage.PREVIEW -> ModelReleaseStage.PREVIEW
      }
    preference =
      when (aiCorePreference) {
        AiCorePreference.FAST -> ModelPreference.FAST
        AiCorePreference.FULL -> ModelPreference.FULL
      }
  }
}
