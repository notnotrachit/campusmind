package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig
import kotlinx.coroutines.delay

private const val MAX_PROMPT_CHARS = 700
private const val MAX_GENERATION_ATTEMPTS = 3

class SingleModelRunner(
  private val configProvider: suspend () -> ModelConfig,
  private val engine: LocalLlmEngine,
) {
  suspend fun generate(prompt: String): Result<String> =
    withRetries { config -> engine.generate(prompt.compactForLiteRt(), config.toOptions()) }

  /** Generate JSON constrained to [schema], so callers receive structured output. */
  suspend fun generateStructured(prompt: String, schema: StructuredSchema): Result<String> =
    withRetries { config -> engine.generateStructured(prompt.compactForLiteRt(), schema, config.toOptions()) }

  private suspend fun withRetries(block: suspend (ModelConfig) -> Result<String>): Result<String> {
    var lastFailure: Throwable? = null

    repeat(MAX_GENERATION_ATTEMPTS) { attempt ->
      val config = configProvider()
      val init = engine.initialize(config)
      if (init.isFailure) {
        lastFailure = init.exceptionOrNull() ?: IllegalStateException("Model initialization failed")
      } else {
        val result = block(config)
        if (result.isSuccess) return result
        lastFailure = result.exceptionOrNull()
      }

      if (attempt < MAX_GENERATION_ATTEMPTS - 1) {
        delay(250L * (attempt + 1))
      }
    }

    return Result.failure(lastFailure ?: IllegalStateException("Model generation failed"))
  }

  private fun ModelConfig.toOptions(): GenerationOptions =
    GenerationOptions(maxTokens = maxTokens, temperature = temperature, topK = topK, topP = topP)

  private fun String.compactForLiteRt(): String {
    val normalized = lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .joinToString(" ")

    return if (normalized.length <= MAX_PROMPT_CHARS) {
      normalized
    } else {
      normalized.take(MAX_PROMPT_CHARS) + "..."
    }
  }
}
