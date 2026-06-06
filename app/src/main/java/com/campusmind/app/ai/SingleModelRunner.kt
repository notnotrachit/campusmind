package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig
import kotlinx.coroutines.delay

private const val MAX_PROMPT_CHARS = 700
private const val MAX_GENERATION_ATTEMPTS = 3

class SingleModelRunner(
  private val configProvider: suspend () -> ModelConfig,
  private val engine: LocalLlmEngine,
) {
  suspend fun generate(prompt: String): Result<String> {
    var lastFailure: Throwable? = null

    repeat(MAX_GENERATION_ATTEMPTS) { attempt ->
      val config = configProvider()
      val init = engine.initialize(config)
      if (init.isFailure) {
        lastFailure = init.exceptionOrNull() ?: IllegalStateException("Model initialization failed")
      } else {
        val result = engine.generate(
          prompt = prompt.compactForLiteRt(),
          options = GenerationOptions(
            maxTokens = config.maxTokens,
            temperature = config.temperature,
            topK = config.topK,
            topP = config.topP,
          ),
        )
        if (result.isSuccess) return result
        lastFailure = result.exceptionOrNull()
      }

      if (attempt < MAX_GENERATION_ATTEMPTS - 1) {
        delay(250L * (attempt + 1))
      }
    }

    return Result.failure(lastFailure ?: IllegalStateException("Model generation failed"))
  }

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
