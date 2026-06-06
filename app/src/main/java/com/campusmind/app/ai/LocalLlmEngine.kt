package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig

interface LocalLlmEngine {
  val isReady: Boolean
  suspend fun initialize(config: ModelConfig): Result<Unit>
  suspend fun generate(prompt: String, options: GenerationOptions = GenerationOptions()): Result<String>

  /**
   * Generate a response constrained to [schema], returning the model's structured tool-call
   * arguments serialized as a JSON object string. Falls back to raw text only if the model
   * does not emit a tool call.
   */
  suspend fun generateStructured(
    prompt: String,
    schema: StructuredSchema,
    options: GenerationOptions = GenerationOptions(),
  ): Result<String>
}

data class GenerationOptions(
  val maxTokens: Int = 512,
  val temperature: Float = 0.4f,
  val topK: Int = 40,
  val topP: Float = 0.95f,
)
