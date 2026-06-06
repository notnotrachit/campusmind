package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType

class StubLocalLlmEngine : LocalLlmEngine {
  override val runtimeType: RuntimeType = RuntimeType.STUB
  override var isReady: Boolean = false
    private set

  override suspend fun initialize(config: ModelConfig): Result<Unit> {
    isReady = true
    return Result.success(Unit)
  }

  override suspend fun generate(prompt: String, options: GenerationOptions): Result<String> {
    val response = "Local stub response for: ${prompt.take(80)}"
    return Result.success(response)
  }
}
