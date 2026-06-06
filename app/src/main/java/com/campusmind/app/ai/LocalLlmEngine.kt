package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType

interface LocalLlmEngine {
  val runtimeType: RuntimeType
  val isReady: Boolean
  suspend fun initialize(config: ModelConfig): Result<Unit>
  suspend fun generate(prompt: String, options: GenerationOptions = GenerationOptions()): Result<String>
}

data class GenerationOptions(
  val maxTokens: Int = 512,
  val temperature: Float = 0.4f,
  val topK: Int = 40,
)
