package com.campusmind.app.ai

import android.content.Context
import com.campusmind.app.model.ModelConfig

class LiteRtLocalLlmEngine(
  private val context: Context,
) : LocalLlmEngine {
  override var isReady: Boolean = false
    private set

  override suspend fun initialize(config: ModelConfig): Result<Unit> {
    if (config.modelPath.isBlank()) {
      isReady = false
      return Result.failure(IllegalArgumentException("Model path is required"))
    }

    // Google AI Edge Gallery initializes LiteRT LM with EngineConfig, Backend, Engine,
    // and Conversation. Keep this class as the app-owned integration point so the Gallery
    // model-manager UI does not leak into CampusMind.
    isReady = false
    return Result.failure(
      NotImplementedError(
        "LiteRT LM runtime wiring is pending device/model validation. Context: ${context.packageName}",
      ),
    )
  }

  override suspend fun generate(prompt: String, options: GenerationOptions): Result<String> =
    Result.failure(NotImplementedError("LiteRT LM generation is not wired yet"))
}
